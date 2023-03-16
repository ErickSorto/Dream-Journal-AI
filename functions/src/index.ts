/* eslint-disable eol-last */
import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

admin.initializeApp();

const firestore = admin.firestore();

// The time (in minutes) after which unverified users should be deleted
const DELETE_UNVERIFIED_USERS_AFTER = 1;

export const deleteUnverifiedUsers =
functions.pubsub.schedule("every 5 minutes").onRun(async (context) => {
  const currentTime = admin.firestore.Timestamp.now();
  const cutoffTime =
  currentTime.toMillis() - DELETE_UNVERIFIED_USERS_AFTER * 60 * 1000;

  const unverifiedUsersSnapshot = await firestore
    .collection("users")
    .where("emailVerified", "==", false)
    .where("registrationTimestamp", "<=", cutoffTime)
    .get();

  const deletePromises = unverifiedUsersSnapshot.docs.map(async (doc) => {
    const uid = doc.id;
    await admin.auth().deleteUser(uid);
    await doc.ref.delete();
  });

  await Promise.all(deletePromises);

  functions.logger
    .info(`Deleted ${unverifiedUsersSnapshot.size} unverified users.`);
});


export const handleGoogleSignIn =
  functions.auth.user().onCreate(async (user) => {
    if (user.providerData
      .some((provider) => provider.providerId === "google.com")) {
      const newUser = {
        displayName: user.displayName,
        email: user.email,
        emailVerified: true,
        registrationTimestamp: admin.firestore.FieldValue.serverTimestamp(),
        // Add any other fields you need
      };

      await firestore.collection("users").doc(user.uid).set(newUser);
      functions.logger
        .info(`Created new user with UID: ${user.uid} using Google Sign In.`);
    }
  });