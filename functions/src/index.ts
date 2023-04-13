/* eslint-disable eol-last */
import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

admin.initializeApp();

const firestore = admin.firestore();

// The time (in minutes) after which unverified users should be deleted
const DELETE_UNVERIFIED_USERS_AFTER = 60;

export const deleteUnverifiedUsers =
  functions.pubsub.schedule("every 60 minutes").onRun(async (context) => {
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


export const handleUserCreate =
  functions.auth.user().onCreate(async (user) => {
    const isGoogleSignIn = user.providerData
      .some((provider) => provider.providerId === "google.com");

    const newUser = {
      uid: user.uid,
      displayName: user.displayName,
      email: user.email,
      emailVerified: isGoogleSignIn || user.emailVerified,
      registrationTimestamp: admin.firestore.FieldValue.serverTimestamp(),
      // Add any other fields you need
    };

    await firestore.collection("users").doc(user.uid).set(newUser);
    functions.logger
      .info(`Created new user with UID: ${user.uid} using ${isGoogleSignIn ? "Google Sign In" : "email and password"}.`);
  });

export const handleEmailVerificationUpdate =
    functions.firestore.document("users/{userId}").onUpdate(async (change, context) => {
      const beforeUser = change.before.data();
      const afterUser = change.after.data();

      if (!beforeUser.emailVerified && afterUser.emailVerified) {
        functions.logger.info(`Updated emailVerified for UID: ${context.params.userId} to true.`);
      }
    });