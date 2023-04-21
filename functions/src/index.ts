/* eslint-disable eol-last */
import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import * as corsFactory from "cors";

admin.initializeApp();

const firestore = admin.firestore();
const cors = corsFactory({ origin: true });

// The time (in minutes) after which unverified users should be deleted
const DELETE_UNVERIFIED_USERS_AFTER = 60;

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

      if (!userRecord.emailVerified) {
        await admin.auth().deleteUser(uid);
        await doc.ref.delete();
        functions.logger.info(`Deleted unverified user with UID: ${uid}.`);
      }
    });

    await Promise.all(deletePromises);

    functions.logger.info(`Processed ${usersSnapshot.size} users.`);
  });


export const handleUserCreate = functions.auth.user().onCreate(async (user) => {
    const isGoogleSignIn = user.providerData
        .some((provider) => provider.providerId === "google.com");
    const newUser = {
        uid: user.uid,
        displayName: user.displayName,
        email: user.email,
        emailVerified: isGoogleSignIn || user.emailVerified,
        registrationTimestamp: admin.firestore.FieldValue.serverTimestamp(),
        dreamTokens: 10, // Add the dreamTokens field with an initial value of 10
        // Add any other fields you need
    };
    await firestore.collection("users").doc(user.uid).set(newUser);
    functions.logger
        .info(`Created new user with UID: ${user.uid} using ${isGoogleSignIn ? "Google Sign In" : "email and password"}.`);
});

exports.handleEmailVerificationUpdate = functions.auth.user().onUpdate(async (userRecord, context) => {
    const { uid } = userRecord;
    const userEmailVerified = userRecord.emailVerified;

    const userDoc = admin.firestore().collection("users").doc(uid);
    const userDocSnapshot = await userDoc.get();
    const userData = userDocSnapshot.data();

    if (userData && !userData.emailVerified && userEmailVerified) {
        functions.logger.info(`Updated emailVerified for UID: ${uid} to true.`);

        await userDoc.update({ emailVerified: true });
    }
});

exports.handlePurchaseVerification = functions.https.onRequest(async (req, res) => {
  cors(req, res, async () => {
    const data = req.body.data;
    const userId = data.userId;
    const dreamTokens = data.dreamTokens;

    console.log(`handlePurchaseVerification - userId: ${userId}, dreamTokens: ${dreamTokens}`);

    const isPurchaseValid = true; // This should be the result of your purchase verification process

    if (isPurchaseValid) {
      // Update the user's dreamTokens in Firestore
      const userRef = firestore.collection("users").doc(userId);
      await userRef.update({
        dreamTokens: admin.firestore.FieldValue.increment(dreamTokens),
      });

      res.status(200).json({ result: { success: true } });
    } else {
      res.status(200).json({ result: { success: false } });
    }
  });
});
