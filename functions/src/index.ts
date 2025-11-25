/* eslint-disable eol-last */
import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import express from "express";
import cors from "cors";
import * as nodemailer from "nodemailer";
import { google } from "googleapis";
import { SecretManagerServiceClient } from '@google-cloud/secret-manager';
import axios from "axios";

admin.initializeApp();

const firestore = admin.firestore();
const app = express();
app.use(cors({ origin: true }));
app.use(express.json());

const DELETE_UNVERIFIED_USERS_AFTER = 60;
const client = new SecretManagerServiceClient();

async function getSecret(name: string): Promise<string> {
    const secretName = `projects/${process.env.GCLOUD_PROJECT}/secrets/${name}/versions/latest`;
    const [version] = await client.accessSecretVersion({ name: secretName });
    if (!version.payload || !version.payload.data) {
        throw new Error(`Secret ${name} payload is null or undefined.`);
    }
    return version.payload.data.toString();
}

exports.getOpenAISecretKey = functions.https.onCall(async (data, context) => {
    try {
        const secretKey = await getSecret("OPENAI_SECRET_KEY");
        return { apiKey: secretKey };
    } catch (error) {
        if (error instanceof Error) {
            console.error('Error retrieving API key:', error.message);
            throw new functions.https.HttpsError('internal', 'Unable to retrieve API key', error.message);
        } else {
            throw new functions.https.HttpsError('internal', 'Unable to retrieve API key');
        }
    }
});

exports.getGeminiApiKey = functions.https.onCall(async (data, context) => {
    try {
        const secretKey = await getSecret("GEMENI_SECRET_KEY");
        return { apiKey: secretKey };
    } catch (error) {
        throw new functions.https.HttpsError('internal', 'Unable to retrieve API key');
    }
});

exports.transcribeAudio = functions.runWith({
    timeoutSeconds: 300,
    memory: '1GB'
}).https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'The function must be called while authenticated.');
    }

    const storagePath = data.storagePath;
    if (!storagePath) {
        throw new functions.https.HttpsError('invalid-argument', 'The function must be called with a storagePath.');
    }

    try {
        const bucket = admin.storage().bucket();
        const file = bucket.file(storagePath);
        const [exists] = await file.exists();
        if (!exists) {
            throw new functions.https.HttpsError('not-found', 'Audio file not found.');
        }
        const [buffer] = await file.download();
        const base64Audio = buffer.toString('base64');

        const apiKey = await getSecret("GEMENI_SECRET_KEY");
        const model = "gemini-2.5-flash";
        const url = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${apiKey}`;

        const response = await axios.post(url, {
            contents: [{
                parts: [
                    { text: "Generate a transcript of the speech." },
                    {
                        inline_data: {
                            mime_type: "audio/mp4",
                            data: base64Audio
                        }
                    }
                ]
            }]
        });

        const text = response.data?.candidates?.[0]?.content?.parts?.[0]?.text;

        if (!text) {
            throw new Error("Empty response from Gemini");
        }

        return { text: text };

    } catch (error) {
        console.error("Transcription error:", error);
        if (axios.isAxiosError(error)) {
             console.error("Axios error details:", error.response?.data);
        }
        throw new functions.https.HttpsError('internal', 'Transcription failed', error);
    }
});

export const deleteUnverifiedUsers =
  functions.pubsub.schedule("every 60 minutes").onRun(async () => {
    const currentTime = admin.firestore.Timestamp.now();
    const cutoffTime =
      currentTime.toMillis() - DELETE_UNVERIFIED_USERS_AFTER * 60 * 1000;
    const cutoffTimestamp = admin.firestore.Timestamp.fromMillis(cutoffTime);

    const usersSnapshot = await firestore
      .collection("users")
      .where("registrationTimestamp", "<=", cutoffTimestamp)
      .get();

    const deletePromises = usersSnapshot.docs.map(async (doc) => {
      const uid = doc.id;
      const userRecord = await admin.auth().getUser(uid);

      if (userRecord.providerData.length === 0) {
        return;
      }

      if (!userRecord.emailVerified) {
        await admin.auth().deleteUser(uid);
        await doc.ref.delete();
        functions.logger.info(`Deleted unverified user with UID: ${uid}.`);
      }
    });

    await Promise.all(deletePromises);
    functions.logger.info(`Processed ${usersSnapshot.size} users.`);
  });

export const deleteAnonymousInactiveUsers = functions.pubsub.schedule("every 24 hours").onRun(async () => {
    const currentTime = admin.firestore.Timestamp.now();
    const cutoffTime = currentTime.toMillis() - (30 * 24 * 60 * 60 * 1000);
    const cutoffTimestamp = admin.firestore.Timestamp.fromMillis(cutoffTime);

    const usersSnapshot = await firestore
        .collection("users")
        .where("lastActiveTimestamp", "<=", cutoffTimestamp)
        .get();

    const deletePromises = usersSnapshot.docs.map(async (doc) => {
        const uid = doc.id;
        const userRecord = await admin.auth().getUser(uid);

        if (userRecord.providerData.length === 0) {
            await admin.auth().deleteUser(uid);
            await doc.ref.delete();
            functions.logger.info(`Deleted anonymous user with UID: ${uid} due to inactivity.`);
        }
    });

    await Promise.all(deletePromises);
    functions.logger.info(`Processed ${usersSnapshot.size} users.`);
});

export const deleteExpiredAudio = functions.pubsub.schedule("every 24 hours").onRun(async () => {
    const currentTime = Date.now();
    const cutoffTime = currentTime - (30 * 24 * 60 * 60 * 1000);

    const dreamsSnapshot = await firestore
        .collectionGroup("my_dreams")
        .where("audioUrl", "!=", "")
        .where("isAudioPermanent", "==", false)
        .get();

    const deletePromises = dreamsSnapshot.docs.map(async (doc) => {
        const data = doc.data();
        if (data.audioTimestamp && data.audioTimestamp <= cutoffTime) {
            const bucket = admin.storage().bucket();
            try {
                const dreamId = doc.id;
                const userId = doc.ref.parent.parent?.id;
                
                if (userId) {
                    const filePath = `${userId}/dream_recordings/${dreamId}.m4a`;
                    const file = bucket.file(filePath);
                    
                    const [exists] = await file.exists();
                    if (exists) {
                        await file.delete();
                        functions.logger.info(`Deleted expired audio for dream ${dreamId} of user ${userId}`);
                    }
                }

                await doc.ref.update({
                    audioUrl: "",
                    audioDuration: 0,
                    audioTimestamp: 0
                });
            } catch (error) {
                functions.logger.error(`Error deleting audio for dream ${doc.id}:`, error);
            }
        }
    });

    await Promise.all(deletePromises);
    functions.logger.info(`Processed ${dreamsSnapshot.size} potentially expired audio recordings.`);
});


exports.handleAccountLinking = functions.https.onCall(async (data, context) => {
    const {permanentUid, anonymousUid} = data;

    if (typeof permanentUid !== "string" || permanentUid.trim() === "" ||
        typeof anonymousUid !== "string" || anonymousUid.trim() === "") {
        throw new functions.https.HttpsError("invalid-argument", "The function requires valid UIDs.");
    }

    if (!context.auth || !context.auth.uid) {
        throw new functions.https.HttpsError("unauthenticated", "The function must be called while authenticated.");
    }

    if (!anonymousUid || !permanentUid || context.auth.uid !== permanentUid) {
        throw new functions.https.HttpsError("invalid-argument", "Invalid UIDs or insufficient permissions.");
    }

    try {
        const anonymousDreamsRef = firestore.collection("users").doc(anonymousUid).collection("my_dreams");
        const permanentDreamsRef = firestore.collection("users").doc(permanentUid).collection("my_dreams");
        let lastSnapshot = null;

        do {
            const anonymousDreamsSnapshot: any = await (lastSnapshot ? anonymousDreamsRef.startAfter(lastSnapshot.docs[lastSnapshot.docs.length - 1]).limit(500).get() : anonymousDreamsRef.limit(500).get());
            const batch = firestore.batch();

            anonymousDreamsSnapshot.forEach((doc: any) => {
                const newDreamRef = permanentDreamsRef.doc(doc.id);
                batch.set(newDreamRef, doc.data());
            });

            await batch.commit();
            lastSnapshot = anonymousDreamsSnapshot;
        } while (lastSnapshot && lastSnapshot.size === 500);

        return {success: true, message: "All dreams transferred successfully."};
    } catch (error) {
        throw new functions.https.HttpsError("internal", "Error during account linking process.");
    }
});


export const handleUserCreate = functions.auth.user().onCreate(async (user) => {
    const isAnonymous = user.providerData.length === 0;
    const isGoogleSignIn = user.providerData
        .some((provider) => provider.providerId === "google.com");

    const newUser = {
        uid: user.uid,
        displayName: user.displayName || "Anonymous",
        email: user.email || "",
        emailVerified: isGoogleSignIn || user.emailVerified || false,
        registrationTimestamp: admin.firestore.FieldValue.serverTimestamp(),
        lastActiveTimestamp: admin.firestore.FieldValue.serverTimestamp(),
        dreamTokens: isAnonymous ? 0 : 25, 
    };

    await firestore.collection("users").doc(user.uid).set(newUser);
});

const purchaseVerificationApp = express();
purchaseVerificationApp.use(cors({ origin: true }));
purchaseVerificationApp.use(express.json());

purchaseVerificationApp.post('/', async (req, res) => {
    const data = req.body.data;
    const userId = data.userId;
    const dreamTokens = data.dreamTokens;

    const isPurchaseValid = true; 

    if (isPurchaseValid) {
      const userRef = firestore.collection("users").doc(userId);
      await userRef.update({
        dreamTokens: admin.firestore.FieldValue.increment(dreamTokens),
      });

      res.status(200).json({ result: { success: true } });
    } else {
      res.status(200).json({ result: { success: false } });
    }
});

exports.handlePurchaseVerification = functions.https.onRequest(purchaseVerificationApp);

exports.createAccountAndSendEmailVerification = functions.https.onCall(async (data, context) => {
    const userEmail = data.email;
    const userPassword = data.password;

    try {
        const existingUser = await admin.auth().getUserByEmail(userEmail);
        if (existingUser.emailVerified) {
            return { message: "Account exists already" };
        } else {
            const verificationLink = await admin.auth().generateEmailVerificationLink(userEmail);
            await sendVerificationEmail(userEmail, verificationLink);
            return { message: "Verification email sent!" };
        }
    } catch (error) {
        if ((error as any).code === "auth/user-not-found") {
            await admin.auth().createUser({
                email: userEmail,
                password: userPassword,
                emailVerified: false
            });

            const verificationLink = await admin.auth().generateEmailVerificationLink(userEmail);
            await sendVerificationEmail(userEmail, verificationLink);
            return { message: "Verification email sent and account created! Please verify email to log in." };
        } else {
            throw new functions.https.HttpsError("internal", "Unable to create account or send verification email");
        }
    }
});

async function sendVerificationEmail(userEmail: string, verificationLink: string) {
    const emailConfig = functions.config().email || {};
    const appEmail = emailConfig.credentials;

    const oauthConfig = functions.config().oauth || {};
    const clientID = oauthConfig.client_id;
    const clientSecret = oauthConfig.client_secret;

    const refreshTokenConfig = functions.config().refreshtoken || {};
    const refreshToken = refreshTokenConfig.token;

    const oauth2Client = new google.auth.OAuth2(
        clientID,
        clientSecret,
        "https://developers.google.com/oauthplayground"
    );

    oauth2Client.setCredentials({
        refresh_token: refreshToken
    });

    const result = await oauth2Client.getAccessToken();
    const token = result.token;

    const transporter = nodemailer.createTransport({
        host: "smtp.gmail.com",
        port: 465,
        secure: true,
        auth: {
            type: "OAuth2",
            user: appEmail,
            clientId: clientID,
            clientSecret: clientSecret,
            refreshToken: refreshToken,
            accessToken: token
        }
    } as nodemailer.TransportOptions);

    await transporter.sendMail({
        from: `"DreamNorth" <${appEmail}>`,
        to: userEmail,
        subject: "Complete Your Registration with DreamNorth",
        text: `Welcome to DreamNorth!\n\nThanks for signing up. Please verify your email by clicking on this link:
        ${verificationLink}\n\nIf you did not sign up for a DreamNorth account, you can safely
        ignore this email.\n\nBest,\nDreamNorth Team`,
        html: `
        <div style="font-family: Arial, sans-serif; padding: 20px;">
            <h2>Welcome to DreamNorth!</h2>
            <p>Thanks for signing up. Please click the button below to verify your email address and complete your registration.</p>
            <a href="${verificationLink}" style="background-color: #4CAF50; color: white;
             padding: 14px 20px; text-align: center; text-decoration: none; display: inline-block; border-radius: 4px;">Verify Email</a>
            <p>If you did not sign up for a DreamNorth account, you can safely ignore this email.</p>
            <p>Best,<br>DreamNorth Team</p>
            <hr>
            <p style="font-size: 0.8em;">If the button above doesn't work, copy and paste this link into your browser:
             <a href="${verificationLink}">${verificationLink}</a></p>
        </div>
        `
    });
}
