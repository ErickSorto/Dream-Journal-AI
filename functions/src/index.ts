/* eslint-disable eol-last */
import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import * as crypto from "crypto";
import express from "express";
import cors from "cors";
import axios from "axios";
import * as nodemailer from "nodemailer";
import { google } from "googleapis";
import { SecretManagerServiceClient } from "@google-cloud/secret-manager";
import { GoogleGenerativeAI, HarmCategory, HarmBlockThreshold } from "@google/generative-ai";

admin.initializeApp();

const firestore = admin.firestore();
const app = express();
app.use(cors({ origin: true }));
app.use(express.json());

const DELETE_UNVERIFIED_USERS_AFTER = 60;
const secretClient = new SecretManagerServiceClient();

async function getSecret(name: string): Promise<string> {
    const secretName = `projects/${process.env.GCLOUD_PROJECT}/secrets/${name}/versions/latest`;
    const [version] = await secretClient.accessSecretVersion({ name: secretName });
    if (!version.payload || !version.payload.data) {
        throw new Error(`Secret ${name} payload is null or undefined.`);
    }
    return version.payload.data.toString();
}

// Lazy initialization for the Gemini client
let genAI: GoogleGenerativeAI | null = null;
async function getGenAIClient(): Promise<GoogleGenerativeAI> {
    if (genAI === null) {
        const apiKey = await getSecret("GEMENI_SECRET_KEY");
        genAI = new GoogleGenerativeAI(apiKey);
    }
    return genAI;
}

type DreamCategorizationResult = {
    isLucid: boolean;
    isNightmare: boolean;
    isRecurring: boolean;
    isFalseAwakening: boolean;
    lucidity: number;
    vividness: number;
    mood: number;
};

const DAILY_TOKEN_FREE_AWARD = 1;
const DAILY_TOKEN_PREMIUM_AWARD = 2;
const DAILY_TOKEN_STREAK_BONUS_AWARD = 5;
const DAILY_TOKEN_STREAK_BONUS_INTERVAL = 7;
const PREMIUM_ENTITLEMENT_ID = "premium";
const DREAM_TEXT_CIPHER_PREFIX = "dnenc1:";
const DREAM_TEXT_CIPHER_CONTEXT = "DreamNorth:user-dream-text:v1";
const DREAM_TEXT_CIPHER_VERSION = 1;
const DREAM_TEXT_CIPHER_IV_SIZE = 16;
const DREAM_TEXT_CIPHER_TAG_SIZE = 32;
const IMAGE_JOB_STATUS_QUEUED = "queued";
const IMAGE_JOB_STATUS_RUNNING = "running";
const IMAGE_JOB_STATUS_SUCCEEDED = "succeeded";
const IMAGE_JOB_STATUS_FAILED = "failed";
const IMAGE_JOB_TYPE_DREAM = "dream_image";
const IMAGE_JOB_TYPE_WORLD = "dream_world";
const NOTIFICATION_DESTINATION_KEY = "dreamnorth.notification.destination";
const NOTIFICATION_DREAM_ID_KEY = "dreamnorth.notification.dream_id";
const DREAM_ART_STORAGE_PREFIX = "images";
const DREAM_WORLD_STORAGE_PREFIX = "dream_world_paintings";
const OPENAI_CHAT_ENDPOINT = "https://api.openai.com/v1/chat/completions";
const OPENAI_IMAGE_ENDPOINT = "https://api.openai.com/v1/images/generations";

type ImageGenerationJob = {
    id?: string;
    type?: string;
    status?: string;
    targetDreamId?: string;
    targetPaintingId?: string;
    style?: string;
    cost?: number;
    tokensReserved?: boolean;
    tokensRefunded?: boolean;
    details?: string;
    errorCode?: string;
    errorMessage?: string;
    createdAt?: FirebaseFirestore.Timestamp;
    updatedAt?: FirebaseFirestore.Timestamp;
    startedAt?: FirebaseFirestore.Timestamp;
    completedAt?: FirebaseFirestore.Timestamp;
};

type OpenAIImagePayload = {
    imageBytes: Buffer;
    contentType: string;
};

function currentUtcDay(): string {
    return new Date().toISOString().slice(0, 10);
}

function previousUtcDay(day: string): string {
    const date = new Date(`${day}T00:00:00.000Z`);
    date.setUTCDate(date.getUTCDate() - 1);
    return date.toISOString().slice(0, 10);
}

function readNumber(value: unknown, fallback = 0): number {
    if (typeof value === "number" && Number.isFinite(value)) {
        return value;
    }

    if (typeof value === "string" && value.trim() !== "") {
        const parsed = Number(value);
        if (Number.isFinite(parsed)) {
            return parsed;
        }
    }

    return fallback;
}

function dreamTextKeyMaterial(userId: string): Buffer {
    return crypto
        .createHash("sha512")
        .update(`${DREAM_TEXT_CIPHER_CONTEXT}:${userId}`)
        .digest();
}

function encryptDreamText(plainText: string, userId: string): string {
    if (plainText.trim() === "" || plainText.startsWith(DREAM_TEXT_CIPHER_PREFIX) ||
        userId.trim() === "") {
        return plainText;
    }

    const keyMaterial = dreamTextKeyMaterial(userId);
    const aesKey = keyMaterial.subarray(0, 32);
    const macKey = keyMaterial.subarray(32, 64);
    const iv = crypto.randomBytes(DREAM_TEXT_CIPHER_IV_SIZE);
    const cipher = crypto.createCipheriv("aes-256-cbc", aesKey, iv);
    const ciphertext = Buffer.concat([
        cipher.update(Buffer.from(plainText, "utf8")),
        cipher.final(),
    ]);
    const authenticatedData = Buffer.concat([
        Buffer.from([DREAM_TEXT_CIPHER_VERSION]),
        iv,
        ciphertext,
    ]);
    const tag = crypto
        .createHmac("sha256", macKey)
        .update(authenticatedData)
        .digest();

    return DREAM_TEXT_CIPHER_PREFIX +
        Buffer.concat([authenticatedData, tag]).toString("base64");
}

function decryptDreamText(storedText: string, userId: string): string {
    if (!storedText.startsWith(DREAM_TEXT_CIPHER_PREFIX) || userId.trim() === "") {
        return storedText;
    }

    try {
        const payload = Buffer.from(
            storedText.substring(DREAM_TEXT_CIPHER_PREFIX.length),
            "base64"
        );
        if (payload.length <=
            1 + DREAM_TEXT_CIPHER_IV_SIZE + DREAM_TEXT_CIPHER_TAG_SIZE) {
            return storedText;
        }
        if (payload[0] !== DREAM_TEXT_CIPHER_VERSION) {
            return storedText;
        }

        const authenticatedData = payload.subarray(
            0,
            payload.length - DREAM_TEXT_CIPHER_TAG_SIZE
        );
        const storedTag = payload.subarray(payload.length - DREAM_TEXT_CIPHER_TAG_SIZE);
        const keyMaterial = dreamTextKeyMaterial(userId);
        const aesKey = keyMaterial.subarray(0, 32);
        const macKey = keyMaterial.subarray(32, 64);
        const expectedTag = crypto
            .createHmac("sha256", macKey)
            .update(authenticatedData)
            .digest();
        if (storedTag.length !== expectedTag.length ||
            !crypto.timingSafeEqual(storedTag, expectedTag)) {
            return storedText;
        }

        const iv = authenticatedData.subarray(1, 1 + DREAM_TEXT_CIPHER_IV_SIZE);
        const ciphertext = authenticatedData.subarray(1 + DREAM_TEXT_CIPHER_IV_SIZE);
        const decipher = crypto.createDecipheriv("aes-256-cbc", aesKey, iv);
        return Buffer.concat([
            decipher.update(ciphertext),
            decipher.final(),
        ]).toString("utf8");
    } catch (error) {
        functions.logger.warn("Failed to decrypt transferred dream text.", error);
        return storedText;
    }
}

function rekeyDreamTextForTransfer(
    value: unknown,
    anonymousUid: string,
    permanentUid: string
): unknown {
    if (typeof value !== "string" || value.trim() === "") {
        return value;
    }

    const plainText = decryptDreamText(value, anonymousUid);
    return encryptDreamText(plainText, permanentUid);
}

function rekeyDreamForTransfer(
    dreamData: FirebaseFirestore.DocumentData,
    anonymousUid: string,
    permanentUid: string
): FirebaseFirestore.DocumentData {
    return {
        ...dreamData,
        uid: permanentUid,
        title: rekeyDreamTextForTransfer(dreamData.title, anonymousUid, permanentUid),
        content: rekeyDreamTextForTransfer(dreamData.content, anonymousUid, permanentUid),
        audioTranscription: rekeyDreamTextForTransfer(
            dreamData.audioTranscription,
            anonymousUid,
            permanentUid
        ),
    };
}

async function getOpenAISecretKey(): Promise<string> {
    const runtimeSecret = process.env.OPENAI_SECRET_KEY;
    if (typeof runtimeSecret === "string" && runtimeSecret.trim() !== "") {
        return runtimeSecret;
    }

    return getSecret("OPENAI_SECRET_KEY");
}

function readString(value: unknown, fallback = ""): string {
    return typeof value === "string" ? value : fallback;
}

function sanitizePromptText(value: string, fallback: string): string {
    const trimmed = value.replace(/\s+/g, " ").trim();
    return trimmed.length > 0 ? trimmed.slice(0, 3500) : fallback;
}

function imageGenerationStatusFields(
    status: string,
    jobId: string,
    extra: Record<string, unknown> = {}
): Record<string, unknown> {
    return {
        imageGenerationStatus: status,
        imageGenerationJobId: jobId,
        imageGenerationUpdatedAt: Date.now(),
        ...extra,
    };
}

async function openAIChat(
    prompt: string,
    maxTokens: number,
    model = "gpt-5.4-mini",
    reasoningEffort = "low"
): Promise<string> {
    const apiKey = await getOpenAISecretKey();
    const body: Record<string, unknown> = {
        model,
        messages: [{ role: "user", content: prompt }],
        max_completion_tokens: maxTokens,
    };

    if (model.startsWith("gpt-5")) {
        body.reasoning_effort = reasoningEffort;
    }

    const response = await axios.post(
        OPENAI_CHAT_ENDPOINT,
        body,
        {
            headers: { Authorization: `Bearer ${apiKey}` },
            timeout: 120000,
        }
    );

    const content = response.data?.choices?.[0]?.message?.content;
    if (typeof content !== "string" || content.trim() === "") {
        throw new Error(response.data?.error?.message || "OpenAI returned an empty chat response.");
    }

    return content.trim();
}

async function generateDreamTitleText(dreamText: string): Promise<string> {
    const prompt = `Please generate a very short title (max 5 words) for this dream. ` +
        `Optimize for concise length. No quotes. Do not include the word "dream". ` +
        `Dream content: ${sanitizePromptText(dreamText, "A remembered dream")}`;
    return openAIChat(prompt, 80, "gpt-5.4-nano", "none");
}

async function generateDreamImageDetails(dreamText: string, cost: number): Promise<string> {
    const content = sanitizePromptText(dreamText, "A peaceful remembered dream");
    const prompt = cost <= 1 ?
        `In one concise, third-person sentence (12-24 words), describe the dream's setting, mood, and standout objects or figures as a beautiful, luminous image prompt. Keep the dream's meaning intact, but prefer clear readable scenery, graceful composition, gentle atmosphere, and balanced light. Avoid making the scene dark, muddy, gloomy, or underexposed unless the dream clearly requires it. Respond only with the sentence, no labels or markdown.\n\n${content}` :
        `Analyze the dream content. Your goal is to create a single, surreal, beautiful, and coherent image prompt that preserves the dream's core symbols and emotional interpretation.\n\n` +
        `1. Identify key visual symbols from the dream.\n` +
        `2. Choose a dominant, unifying scene.\n` +
        `3. Compose one vivid sentence (16-32 words) that describes a blended scene with graceful composition, luminous atmosphere, balanced exposure, and vivid but tasteful color.\n` +
        `4. Avoid dull, muddy, overly dark, underexposed, or murky output unless darkness is essential to the dream. Even night scenes should have readable moonlight, lantern glow, stars, reflections, or soft rim light.\n\n` +
        `Crucially, ensure the final sentence is safe for image generation. Avoid sensitive terms.\n\n` +
        `Dream Content:\n${content}\n\n` +
        `Respond only with the final image prompt sentence. Do not include analysis, bullets, labels, quotes, or markdown.`;

    return openAIChat(prompt, 2000, cost <= 1 ? "gpt-5.4-mini" : "gpt-5.5", cost <= 1 ? "low" : "high");
}

async function generateDreamWorldSummary(dreams: FirebaseFirestore.DocumentData[], uid: string, cost: number): Promise<string> {
    const dreamContent = dreams.map((dream) => {
        const content = decryptDreamText(readString(dream.content), uid);
        const transcription = decryptDreamText(readString(dream.audioTranscription), uid);
        const transcribed = transcription.trim() !== "" ? `\nTranscription: ${transcription}` : "";
        return `Date: ${readString(dream.date)}\nContent: ${content}${transcribed}`;
    }).join("\n\n");

    const prompt = `Analyze the recurring themes and objects in the following dream entries. ` +
        `Your goal is to create a single, surreal, beautiful, and coherent image prompt that preserves the dream pattern while making the world feel inviting, luminous, and visually rich.\n\n` +
        `1. Identify 4-5 key visual symbols from the dreams.\n` +
        `2. Choose ONE or TWO dominant, unifying scenes from the dreams.\n` +
        `3. Compose one vivid sentence that describes a blended scene. Place the key symbols logically within this combined environment.\n` +
        `4. Prefer balanced exposure, clear readable details, atmospheric glow, layered depth, vivid natural accents, and elegant composition. Avoid dull, muddy, overly dark, or underexposed worlds unless darkness is essential to the dreams.\n\n` +
        `Crucially, ensure the final sentence is safe for image generation. Avoid sensitive terms.\n\n` +
        `Dream Entries:\n${sanitizePromptText(dreamContent, "A collection of remembered dreams")}\n\n` +
        `Respond only with the single, safe, coherent image prompt sentence. Do not include analysis, bullets, labels, quotes, or markdown.`;

    return openAIChat(prompt, 5000, cost <= 1 ? "gpt-5.4-mini" : "gpt-5.5", "high");
}

async function generateOpenAIImage(details: string, style: string, cost: number): Promise<OpenAIImagePayload> {
    const apiKey = await getOpenAISecretKey();
    const beautyDirection = "beautiful dream artwork, luminous balanced exposure, clear readable details, elegant composition, layered depth, soft atmospheric glow, vivid natural color accents, gentle highlights, avoid dull muddy palettes, avoid crushed blacks, avoid overly dark or underexposed lighting unless explicitly required by the dream";
    const finalPrompt = `${sanitizePromptText(details, "A beautiful, peaceful dream scene")}, ${style || "dreamlike cinematic illustration"}, ${beautyDirection}`;
    const primaryModel = cost <= 1 ? "gpt-image-1-mini" : "gpt-image-2";
    const fallbackModel = cost <= 1 ? "gpt-image-1-mini" : "gpt-image-1";

    async function requestImage(model: string): Promise<OpenAIImagePayload> {
        const normalizedModel = model.toLowerCase();
        const response = await axios.post(
            OPENAI_IMAGE_ENDPOINT,
            {
                model: normalizedModel,
                prompt: finalPrompt,
                size: "1024x1024",
                quality: normalizedModel === "gpt-image-1-mini" || cost <= 1 ? "low" : "high",
                n: 1,
            },
            {
                headers: { Authorization: `Bearer ${apiKey}` },
                timeout: 180000,
            }
        );

        const first = response.data?.data?.[0];
        if (typeof first?.b64_json === "string" && first.b64_json.trim() !== "") {
            return {
                imageBytes: Buffer.from(first.b64_json, "base64"),
                contentType: "image/png",
            };
        }

        if (typeof first?.url === "string" && first.url.trim() !== "") {
            const imageResponse = await axios.get(first.url, {
                responseType: "arraybuffer",
                timeout: 120000,
            });
            return {
                imageBytes: Buffer.from(imageResponse.data),
                contentType: imageResponse.headers["content-type"] || "image/png",
            };
        }

        throw new Error(response.data?.error?.message || "OpenAI image response did not include image data.");
    }

    try {
        return await requestImage(primaryModel);
    } catch (error) {
        functions.logger.warn("Primary image model failed; trying fallback.", { primaryModel, fallbackModel, error });
        return requestImage(fallbackModel);
    }
}

async function uploadGeneratedImage(
    uid: string,
    storagePath: string,
    payload: OpenAIImagePayload
): Promise<string> {
    const bucket = admin.storage().bucket();
    const file = bucket.file(storagePath);
    const downloadToken = crypto.randomUUID();
    await file.save(payload.imageBytes, {
        metadata: {
            contentType: payload.contentType,
            cacheControl: "public,max-age=31536000",
            metadata: {
                firebaseStorageDownloadTokens: downloadToken,
            },
        },
    });
    const encodedPath = encodeURIComponent(storagePath);
    return `https://firebasestorage.googleapis.com/v0/b/${bucket.name}/o/${encodedPath}` +
        `?alt=media&token=${downloadToken}&v=${Date.now()}&uid=${encodeURIComponent(uid)}`;
}

async function refundReservedTokensIfNeeded(
    uid: string,
    jobRef: FirebaseFirestore.DocumentReference,
    job: ImageGenerationJob,
    errorCode: string,
    errorMessage: string
): Promise<void> {
    const cost = readNumber(job.cost, 0);
    await firestore.runTransaction(async (transaction) => {
        const latestJobSnap = await transaction.get(jobRef);
        const latestJob = latestJobSnap.data() as ImageGenerationJob | undefined;
        if (!latestJob || latestJob.tokensRefunded === true) {
            return;
        }

        const updates = {
            status: IMAGE_JOB_STATUS_FAILED,
            errorCode,
            errorMessage: errorMessage.slice(0, 500),
            tokensRefunded: true,
            updatedAt: admin.firestore.FieldValue.serverTimestamp(),
            completedAt: admin.firestore.FieldValue.serverTimestamp(),
        };
        transaction.update(jobRef, updates);

        if (cost > 0 && latestJob.tokensReserved === true) {
            transaction.update(firestore.collection("users").doc(uid), {
                dreamTokens: admin.firestore.FieldValue.increment(cost),
            });
        }
    });
}

async function notifyGeneratedImageReady(
    uid: string,
    type: string,
    targetId: string,
    imageUrl: string
): Promise<void> {
    try {
        const tokenSnap = await firestore
            .collection("users")
            .doc(uid)
            .collection("device_tokens")
            .where("active", "==", true)
            .get();
        const tokens = tokenSnap.docs
            .map((doc) => readString(doc.data().token))
            .filter((token) => token.trim() !== "");
        if (tokens.length === 0) {
            return;
        }

        const isWorld = type === IMAGE_JOB_TYPE_WORLD;
        const destination = isWorld ? "paint_dream_world" : "dream_journal";
        const title = isWorld ? "Your dream world is ready" : "Your dream art is ready";
        const body = isWorld ? "Your new world painting has been saved." : "Your generated dream image has been saved.";
        const data: Record<string, string> = {
            [NOTIFICATION_DESTINATION_KEY]: destination,
            title,
            body,
            imageUrl,
            targetId,
            type,
        };
        if (!isWorld) {
            data[NOTIFICATION_DREAM_ID_KEY] = targetId;
        }

        await admin.messaging().sendEachForMulticast({
            tokens,
            data,
            android: {
                priority: "high",
            },
            apns: {
                payload: {
                    aps: {
                        alert: {
                            title,
                            body,
                        },
                        sound: "default",
                        mutableContent: true,
                    },
                },
                fcmOptions: {
                    imageUrl,
                },
            },
        });
    } catch (error) {
        functions.logger.warn("Generated image notification failed.", { uid, type, targetId, error });
    }
}

let revenueCatSecretKey: string | null | undefined;

async function getRevenueCatSecretKey(): Promise<string | null> {
    if (revenueCatSecretKey !== undefined) {
        return revenueCatSecretKey;
    }

    const runtimeSecret = process.env.REVENUECAT_SECRET_KEY;
    if (typeof runtimeSecret === "string" && runtimeSecret.trim() !== "") {
        revenueCatSecretKey = runtimeSecret;
        return revenueCatSecretKey;
    }

    const configSecret = functions.config().revenuecat?.secret_key;
    if (typeof configSecret === "string" && configSecret.trim() !== "") {
        revenueCatSecretKey = configSecret;
        return revenueCatSecretKey;
    }

    try {
        revenueCatSecretKey = await getSecret("REVENUECAT_SECRET_KEY");
        return revenueCatSecretKey;
    } catch (error) {
        functions.logger.warn("RevenueCat secret is not configured; daily premium token bonus disabled.");
        revenueCatSecretKey = null;
        return null;
    }
}

async function hasRevenueCatPremiumEntitlement(uid: string): Promise<boolean> {
    const secretKey = await getRevenueCatSecretKey();
    if (!secretKey) {
        return false;
    }

    try {
        const response = await axios.get(
            `https://api.revenuecat.com/v1/subscribers/${encodeURIComponent(uid)}`,
            { headers: { Authorization: `Bearer ${secretKey}` } }
        );
        const entitlement = response.data?.subscriber?.entitlements?.[PREMIUM_ENTITLEMENT_ID];
        if (!entitlement) {
            return false;
        }

        if (entitlement.expires_date === null) {
            return true;
        }

        const expiresAt = Date.parse(entitlement.expires_date);
        return Number.isFinite(expiresAt) && expiresAt > Date.now();
    } catch (error) {
        functions.logger.error("Failed to verify RevenueCat premium entitlement", { uid, error });
        return false;
    }
}

function parseCategorizationResponse(rawResponse: string): DreamCategorizationResult {
    const normalized = rawResponse
        .trim()
        .replace(/^```json\s*/i, "")
        .replace(/^```\s*/i, "")
        .replace(/\s*```$/, "");

    let parsed: unknown;
    try {
        parsed = JSON.parse(normalized);
    } catch (error) {
        throw new Error("Gemini returned invalid JSON for dream categorization.");
    }

    if (!parsed || typeof parsed !== "object" || Array.isArray(parsed)) {
        throw new Error("Gemini returned a non-object categorization payload.");
    }

    const payload = parsed as Record<string, unknown>;

    return {
        isLucid: readBooleanField(payload, "isLucid"),
        isNightmare: readBooleanField(payload, "isNightmare"),
        isRecurring: readBooleanField(payload, "isRecurring"),
        isFalseAwakening: readBooleanField(payload, "isFalseAwakening"),
        lucidity: readRatingField(payload, "lucidity"),
        vividness: readRatingField(payload, "vividness"),
        mood: readRatingField(payload, "mood"),
    };
}

function readBooleanField(payload: Record<string, unknown>, fieldName: string): boolean {
    const value = payload[fieldName];

    if (typeof value === "boolean") {
        return value;
    }

    if (typeof value === "string") {
        const normalized = value.trim().toLowerCase();
        if (normalized === "true") {
            return true;
        }
        if (normalized === "false") {
            return false;
        }
    }

    throw new Error(`Gemini returned an invalid boolean for ${fieldName}.`);
}

function readRatingField(payload: Record<string, unknown>, fieldName: string): number {
    const value = payload[fieldName];

    let parsed: number | null = null;
    if (typeof value === "number") {
        parsed = value;
    } else if (typeof value === "string" && value.trim() !== "") {
        parsed = Number(value);
    }

    if (parsed === null || !Number.isFinite(parsed)) {
        throw new Error(`Gemini returned an invalid numeric rating for ${fieldName}.`);
    }

    const rounded = Math.round(parsed);
    if (rounded < 1 || rounded > 5) {
        throw new Error(`Gemini returned an out-of-range rating for ${fieldName}.`);
    }

    return rounded;
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

exports.claimDailyDreamTokens = functions.runWith({
    secrets: ["REVENUECAT_SECRET_KEY"],
}).https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError("unauthenticated", "The function must be called while authenticated.");
    }

    const uid = context.auth.uid;
    const authToken = context.auth.token;
    const today = currentUtcDay();
    const yesterday = previousUtcDay(today);
    const hasPremiumEntitlement = await hasRevenueCatPremiumEntitlement(uid);
    const userRef = firestore.collection("users").doc(uid);

    try {
        return await firestore.runTransaction(async (transaction) => {
            const snapshot = await transaction.get(userRef);
            const userData = snapshot.data() || {};
            const lastClaimDay = typeof userData.lastDailyDreamTokenClaimDay === "string" ?
                userData.lastDailyDreamTokenClaimDay :
                typeof userData.lastDailyDreamTokenClaimDate === "string" ?
                    userData.lastDailyDreamTokenClaimDate :
                    null;

            const currentTokens = readNumber(userData.dreamTokens, 0);
            const previousStreak = readNumber(userData.dailyDreamTokenStreak, 0);
            const maxDailyTokens = hasPremiumEntitlement ?
                DAILY_TOKEN_PREMIUM_AWARD :
                DAILY_TOKEN_FREE_AWARD;
            const tokensAlreadyAwardedToday = lastClaimDay === today ?
                readNumber(userData.lastDailyDreamTokensAwarded, DAILY_TOKEN_FREE_AWARD) :
                0;
            const tokensAwarded = Math.max(0, maxDailyTokens - tokensAlreadyAwardedToday);

            if (tokensAwarded <= 0) {
                throw new functions.https.HttpsError("failed-precondition", "Daily token already claimed.");
            }

            const newStreak = lastClaimDay === today ?
                previousStreak :
                lastClaimDay === yesterday ?
                    previousStreak + 1 :
                    1;
            const previousCompletedWeeks = Math.max(
                readNumber(userData.dailyDreamTokenCompletedWeeks, 0),
                Math.floor(previousStreak / DAILY_TOKEN_STREAK_BONUS_INTERVAL)
            );
            const completedWeeks = Math.max(
                previousCompletedWeeks,
                Math.floor(newStreak / DAILY_TOKEN_STREAK_BONUS_INTERVAL)
            );
            const bonusAlreadyAwardedToday = userData.lastDailyDreamTokenBonusDay === today;
            const bonusTokensAwarded = lastClaimDay !== today &&
                newStreak > 0 &&
                newStreak % DAILY_TOKEN_STREAK_BONUS_INTERVAL === 0 &&
                !bonusAlreadyAwardedToday ?
                DAILY_TOKEN_STREAK_BONUS_AWARD :
                0;
            const tokensAwardedToday = tokensAlreadyAwardedToday + tokensAwarded;
            const totalAwarded = tokensAwarded + bonusTokensAwarded;
            const totalTokens = currentTokens + totalAwarded;

            const dailyClaimFields = {
                dreamTokens: totalTokens,
                lastDailyDreamTokenClaimDay: today,
                lastDailyDreamTokenClaimDate: today,
                lastDailyDreamTokenClaimedAt: admin.firestore.FieldValue.serverTimestamp(),
                lastDailyDreamTokensAwarded: tokensAwardedToday,
                lastDailyDreamTokenAllowance: maxDailyTokens,
                dailyDreamTokenStreak: newStreak,
                dailyDreamTokenCompletedWeeks: completedWeeks,
                ...(bonusTokensAwarded > 0 ? {
                    lastDailyDreamTokenBonusDay: today,
                    lastDailyDreamTokenBonusAwardedAt: admin.firestore.FieldValue.serverTimestamp(),
                    lastDailyDreamTokenBonusAward: bonusTokensAwarded,
                } : {}),
            };

            if (snapshot.exists) {
                transaction.update(userRef, dailyClaimFields);
            } else {
                transaction.set(userRef, {
                    uid,
                    displayName: authToken.name || "Dreamer",
                    email: authToken.email || "",
                    emailVerified: authToken.email_verified === true,
                    registrationTimestamp: admin.firestore.FieldValue.serverTimestamp(),
                    lastActiveTimestamp: admin.firestore.FieldValue.serverTimestamp(),
                    unlockedWords: [],
                    ...dailyClaimFields,
                });
            }

            return {
                tokensAwarded: totalAwarded,
                dailyTokensAwarded: tokensAwarded,
                bonusTokensAwarded,
                totalTokens,
                streak: newStreak,
                completedWeeks,
                claimDay: today,
                tokensAwardedToday,
                dailyTokenAllowance: maxDailyTokens,
            };
        });
    } catch (error) {
        if (error instanceof functions.https.HttpsError) {
            throw error;
        }
        functions.logger.error("Failed to claim daily dream tokens", error);
        throw new functions.https.HttpsError("internal", "Failed to claim daily token.");
    }
});

exports.stampDreamServerDay = functions.firestore
    .document("users/{uid}/my_dreams/{dreamId}")
    .onWrite(async (change) => {
        if (!change.after.exists) {
            return null;
        }

        const afterData = change.after.data() || {};
        const beforeData = change.before.exists ? change.before.data() || {} : null;
        const beforeServerDay = beforeData && typeof beforeData.serverDreamDay === "string" ?
            beforeData.serverDreamDay :
            null;

        if (!change.before.exists) {
            const updates: Record<string, unknown> = {};
            if (typeof afterData.serverDreamDay !== "string" || afterData.serverDreamDay.trim() === "") {
                updates.serverDreamDay = currentUtcDay();
            }
            if (!afterData.serverDreamCreatedAt) {
                updates.serverDreamCreatedAt = admin.firestore.FieldValue.serverTimestamp();
            }

            return Object.keys(updates).length > 0 ? change.after.ref.update(updates) : null;
        }

        if (beforeServerDay && afterData.serverDreamDay !== beforeServerDay) {
            return change.after.ref.update({
                serverDreamDay: beforeServerDay,
                serverDreamCreatedAt: beforeData?.serverDreamCreatedAt || admin.firestore.FieldValue.serverTimestamp(),
            });
        }

        return null;
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

        const client = await getGenAIClient();
        const model = client.getGenerativeModel({ model: "gemini-1.5-flash" });

        const audioPart = {
            inlineData: {
                data: base64Audio,
                mimeType: "audio/mp4",
            },
        };

        const result = await model.generateContent(["Generate a transcript of the speech.", audioPart]);
        const text = result.response.text();

        if (!text) {
            throw new Error("Empty response from Gemini");
        }

        return { text: text };

    } catch (error) {
        functions.logger.error("Transcription error:", error);
        throw new functions.https.HttpsError('internal', 'Transcription failed', error);
    }
});

exports.categorizeDream = functions.https.onCall(async (data, context) => {
    functions.logger.info("Starting categorizeDream function", { structuredData: true });

    if (!context.auth) {
        functions.logger.warn("Unauthenticated call to categorizeDream");
        throw new functions.https.HttpsError('unauthenticated', 'The function must be called while authenticated.');
    }

    const dreamContent = data.dreamContent;
    if (!dreamContent || typeof dreamContent !== 'string' || dreamContent.trim().length === 0) {
        functions.logger.error("Invalid dreamContent argument", {
            dreamContentType: typeof dreamContent,
            hasDreamContent: Boolean(dreamContent),
        });
        throw new functions.https.HttpsError('invalid-argument', 'The function must be called with valid dreamContent.');
    }

    try {
        const client = await getGenAIClient();
        const model = client.getGenerativeModel({
            model: "gemini-2.5-flash",
            generationConfig: { responseMimeType: "application/json" },
            safetySettings: [
                { category: HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT, threshold: HarmBlockThreshold.BLOCK_NONE },
                { category: HarmCategory.HARM_CATEGORY_HARASSMENT, threshold: HarmBlockThreshold.BLOCK_NONE },
                { category: HarmCategory.HARM_CATEGORY_HATE_SPEECH, threshold: HarmBlockThreshold.BLOCK_NONE },
                { category: HarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT, threshold: HarmBlockThreshold.BLOCK_NONE },
            ]
        });


        const prompt = `
            Analyze the following dream content and return a JSON object with the specified structure.
            - isLucid: boolean (true if the dream is lucid)
            - isNightmare: boolean (true if it is a nightmare)
            - isRecurring: boolean (true if it seems recurring)
            - isFalseAwakening: boolean (true if it involves a false awakening)
            - lucidity: integer (a score from 1-5 for lucidity)
            - vividness: integer (a score from 1-5 for vividness)
            - mood: integer (a score from 1-5 for mood, 1 being very negative, 5 being very positive)

            Only return the JSON object, nothing else.

            Dream Content: "${dreamContent}"
        `;

        functions.logger.info("Sending prompt to Gemini API");

        const result = await model.generateContent(prompt);
        const rawResponse = result.response.text();

        if (!rawResponse) {
            functions.logger.error("Empty response from Gemini");
            throw new Error("Empty response from Gemini");
        }

        const finalResult = parseCategorizationResponse(rawResponse);

        functions.logger.info("Returning result:", { result: finalResult });
        return finalResult;

    } catch (error) {
        functions.logger.error("Error in categorizeDream:", error);
        throw new functions.https.HttpsError('internal', 'Dream categorization failed', error);
    }
});
exports.generateDreamTitle = functions.runWith({
    timeoutSeconds: 120,
    memory: "512MB",
}).https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError("unauthenticated", "The function must be called while authenticated.");
    }

    const dreamContent = readString(data?.dreamContent).trim();
    if (dreamContent.length === 0) {
        throw new functions.https.HttpsError("invalid-argument", "dreamContent is required.");
    }

    try {
        const title = await generateDreamTitleText(dreamContent);
        return { title };
    } catch (error) {
        functions.logger.error("Dream title generation failed.", { uid: context.auth.uid, error });
        throw new functions.https.HttpsError("internal", "Dream title generation failed.");
    }
});

exports.enqueueDreamImageGeneration = functions.https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError("unauthenticated", "The function must be called while authenticated.");
    }

    const uid = context.auth.uid;
    const dreamId = readString(data?.dreamId).trim();
    const style = readString(data?.style, "dreamlike cinematic illustration").trim();
    const cost = Math.max(0, Math.round(readNumber(data?.cost, 0)));
    if (dreamId.length === 0) {
        throw new functions.https.HttpsError("invalid-argument", "dreamId is required.");
    }

    const userRef = firestore.collection("users").doc(uid);
    const dreamRef = userRef.collection("my_dreams").doc(dreamId);
    const jobRef = userRef.collection("image_generation_jobs").doc();

    try {
        await firestore.runTransaction(async (transaction) => {
            const userSnap = await transaction.get(userRef);
            const dreamSnap = await transaction.get(dreamRef);

            if (!dreamSnap.exists) {
                throw new functions.https.HttpsError("not-found", "Dream not found.");
            }

            const currentTokens = readNumber(userSnap.data()?.dreamTokens, 0);
            if (currentTokens < cost) {
                throw new functions.https.HttpsError("failed-precondition", "Not enough dream tokens available.");
            }

            if (cost > 0) {
                transaction.update(userRef, {
                    dreamTokens: admin.firestore.FieldValue.increment(-cost),
                });
            }

            transaction.set(jobRef, {
                id: jobRef.id,
                type: IMAGE_JOB_TYPE_DREAM,
                status: IMAGE_JOB_STATUS_QUEUED,
                targetDreamId: dreamId,
                style,
                cost,
                tokensReserved: cost > 0,
                tokensRefunded: false,
                createdAt: admin.firestore.FieldValue.serverTimestamp(),
                updatedAt: admin.firestore.FieldValue.serverTimestamp(),
            });

            transaction.update(
                dreamRef,
                imageGenerationStatusFields(IMAGE_JOB_STATUS_QUEUED, jobRef.id, {
                    imageGenerationStartedAt: Date.now(),
                }) as FirebaseFirestore.UpdateData<FirebaseFirestore.DocumentData>
            );
        });

        return { jobId: jobRef.id };
    } catch (error) {
        if (error instanceof functions.https.HttpsError) {
            throw error;
        }
        functions.logger.error("Failed to enqueue dream image generation.", { uid, dreamId, error });
        throw new functions.https.HttpsError("internal", "Failed to enqueue dream image generation.");
    }
});

exports.enqueueDreamWorldPaintingGeneration = functions.https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError("unauthenticated", "The function must be called while authenticated.");
    }

    const uid = context.auth.uid;
    const style = readString(data?.style, "dreamlike cinematic illustration").trim();
    const cost = Math.max(0, Math.round(readNumber(data?.cost, 0)));
    const userRef = firestore.collection("users").doc(uid);
    const paintingRef = userRef.collection("dream_world_paintings").doc();
    const jobRef = userRef.collection("image_generation_jobs").doc();

    try {
        await firestore.runTransaction(async (transaction) => {
            const userSnap = await transaction.get(userRef);
            const currentTokens = readNumber(userSnap.data()?.dreamTokens, 0);
            if (currentTokens < cost) {
                throw new functions.https.HttpsError("failed-precondition", "Not enough dream tokens available.");
            }

            if (cost > 0) {
                transaction.update(userRef, {
                    dreamTokens: admin.firestore.FieldValue.increment(-cost),
                });
            }

            transaction.set(paintingRef, {
                id: paintingRef.id,
                userId: uid,
                imageUrl: "",
                description: "",
                timestamp: Date.now(),
                date: currentUtcDay(),
                status: IMAGE_JOB_STATUS_QUEUED,
                jobId: jobRef.id,
                createdAt: Date.now(),
                updatedAt: Date.now(),
            });

            transaction.set(jobRef, {
                id: jobRef.id,
                type: IMAGE_JOB_TYPE_WORLD,
                status: IMAGE_JOB_STATUS_QUEUED,
                targetPaintingId: paintingRef.id,
                style,
                cost,
                tokensReserved: cost > 0,
                tokensRefunded: false,
                createdAt: admin.firestore.FieldValue.serverTimestamp(),
                updatedAt: admin.firestore.FieldValue.serverTimestamp(),
            });
        });

        return { jobId: jobRef.id, paintingId: paintingRef.id };
    } catch (error) {
        if (error instanceof functions.https.HttpsError) {
            throw error;
        }
        functions.logger.error("Failed to enqueue dream world generation.", { uid, error });
        throw new functions.https.HttpsError("internal", "Failed to enqueue dream world generation.");
    }
});

exports.processImageGenerationJob = functions.runWith({
    timeoutSeconds: 540,
    memory: "1GB",
}).firestore
    .document("users/{uid}/image_generation_jobs/{jobId}")
    .onWrite(async (change, context) => {
        if (!change.after.exists) {
            return null;
        }

        const uid = context.params.uid;
        const jobId = context.params.jobId;
        const jobRef = change.after.ref;
        const initialJob = change.after.data() as ImageGenerationJob;
        if (initialJob.status !== IMAGE_JOB_STATUS_QUEUED) {
            return null;
        }

        let claimedJob: ImageGenerationJob | undefined;
        await firestore.runTransaction(async (transaction) => {
            const snap = await transaction.get(jobRef);
            const latest = snap.data() as ImageGenerationJob | undefined;
            if (!latest || latest.status !== IMAGE_JOB_STATUS_QUEUED) {
                return;
            }

            claimedJob = latest;
            transaction.update(jobRef, {
                status: IMAGE_JOB_STATUS_RUNNING,
                startedAt: admin.firestore.FieldValue.serverTimestamp(),
                updatedAt: admin.firestore.FieldValue.serverTimestamp(),
            });
        });

        const job = claimedJob;
        if (!job) {
            return null;
        }

        try {
            if (job.type === IMAGE_JOB_TYPE_DREAM) {
                const dreamId = readString(job.targetDreamId);
                const dreamRef = firestore.collection("users").doc(uid).collection("my_dreams").doc(dreamId);
                const dreamSnap = await dreamRef.get();
                if (!dreamSnap.exists) {
                    throw new Error("target-deleted:dream");
                }

                const dream = dreamSnap.data() || {};
                await dreamRef.update(imageGenerationStatusFields(IMAGE_JOB_STATUS_RUNNING, jobId));
                const content = decryptDreamText(readString(dream.content), uid);
                const transcription = decryptDreamText(readString(dream.audioTranscription), uid);
                const fullContent = transcription.trim() !== "" ?
                    `${content}\n\nAudio Transcription:\n${transcription}` :
                    content;
                const details = await generateDreamImageDetails(fullContent, readNumber(job.cost, 0));
                const imagePayload = await generateOpenAIImage(details, readString(job.style), readNumber(job.cost, 0));
                const imageUrl = await uploadGeneratedImage(
                    uid,
                    `${DREAM_ART_STORAGE_PREFIX}/${uid}/${dreamId}.jpg`,
                    imagePayload
                );

                await dreamRef.update({
                    generatedImage: imageUrl,
                    generatedDetails: details,
                    ...imageGenerationStatusFields(IMAGE_JOB_STATUS_SUCCEEDED, jobId, {
                        imageGenerationCompletedAt: Date.now(),
                    }),
                });
                await jobRef.update({
                    status: IMAGE_JOB_STATUS_SUCCEEDED,
                    details,
                    imageUrl,
                    completedAt: admin.firestore.FieldValue.serverTimestamp(),
                    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
                });
                await notifyGeneratedImageReady(uid, IMAGE_JOB_TYPE_DREAM, dreamId, imageUrl);
                return null;
            }

            if (job.type === IMAGE_JOB_TYPE_WORLD) {
                const paintingId = readString(job.targetPaintingId);
                const userRef = firestore.collection("users").doc(uid);
                const paintingRef = userRef.collection("dream_world_paintings").doc(paintingId);
                const paintingSnap = await paintingRef.get();
                if (!paintingSnap.exists) {
                    throw new Error("target-deleted:painting");
                }

                await paintingRef.update({
                    status: IMAGE_JOB_STATUS_RUNNING,
                    updatedAt: Date.now(),
                });

                const dreamsSnap = await userRef
                    .collection("my_dreams")
                    .orderBy("timestamp", "desc")
                    .limit(15)
                    .get();
                if (dreamsSnap.size < 3) {
                    throw new Error("not-enough-dreams");
                }

                const summary = await generateDreamWorldSummary(
                    dreamsSnap.docs.map((doc) => doc.data()),
                    uid,
                    readNumber(job.cost, 0)
                );
                const imagePayload = await generateOpenAIImage(summary, readString(job.style), readNumber(job.cost, 0));
                const imageUrl = await uploadGeneratedImage(
                    uid,
                    `${uid}/${DREAM_WORLD_STORAGE_PREFIX}/${paintingId}.jpg`,
                    imagePayload
                );

                await paintingRef.update({
                    imageUrl,
                    description: summary,
                    status: IMAGE_JOB_STATUS_SUCCEEDED,
                    updatedAt: Date.now(),
                    completedAt: Date.now(),
                });
                await userRef.update({ hasGeneratedDreamWorld: true });
                await jobRef.update({
                    status: IMAGE_JOB_STATUS_SUCCEEDED,
                    details: summary,
                    imageUrl,
                    completedAt: admin.firestore.FieldValue.serverTimestamp(),
                    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
                });
                await notifyGeneratedImageReady(uid, IMAGE_JOB_TYPE_WORLD, paintingId, imageUrl);
                return null;
            }

            throw new Error(`unsupported-job-type:${job.type || ""}`);
        } catch (error) {
            const message = error instanceof Error ? error.message : "Unknown image generation error.";
            functions.logger.error("Image generation job failed.", { uid, jobId, message, error });
            await refundReservedTokensIfNeeded(uid, jobRef, job, "generation_failed", message);

            if (job.type === IMAGE_JOB_TYPE_DREAM && job.targetDreamId) {
                await firestore.collection("users").doc(uid).collection("my_dreams").doc(job.targetDreamId)
                    .update(imageGenerationStatusFields(IMAGE_JOB_STATUS_FAILED, jobId, {
                        imageGenerationErrorCode: "generation_failed",
                    }))
                    .catch((updateError) => functions.logger.warn("Failed to mark dream image job failed.", updateError));
            } else if (job.type === IMAGE_JOB_TYPE_WORLD && job.targetPaintingId) {
                await firestore.collection("users").doc(uid).collection("dream_world_paintings").doc(job.targetPaintingId)
                    .update({
                        status: IMAGE_JOB_STATUS_FAILED,
                        errorCode: "generation_failed",
                        updatedAt: Date.now(),
                    })
                    .catch((updateError) => functions.logger.warn("Failed to mark world image job failed.", updateError));
            }
            return null;
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
        const anonymousUserRef = firestore.collection("users").doc(anonymousUid);
        const permanentUserRef = firestore.collection("users").doc(permanentUid);
        const anonymousUserSnapshot = await anonymousUserRef.get();
        const anonymousUserData = anonymousUserSnapshot.data() || {};
        const anonymousDreamsRef = firestore.collection("users").doc(anonymousUid).collection("my_dreams");
        const permanentDreamsRef = firestore.collection("users").doc(permanentUid).collection("my_dreams");
        let lastSnapshot = null;

        if (anonymousUserData.hasCompletedOnboarding === true) {
            await permanentUserRef.set(
                {
                    hasCompletedOnboarding: true,
                    onboardingCompletionMode:
                        anonymousUserData.onboardingCompletionMode || "basic_mode",
                    onboardingCompletedAt:
                        anonymousUserData.onboardingCompletedAt ||
                        admin.firestore.FieldValue.serverTimestamp(),
                    onboardingTransferredFromAnonymousUid: anonymousUid,
                    onboardingTransferredAt: admin.firestore.FieldValue.serverTimestamp(),
                },
                { merge: true }
            );
        }

        do {
            const anonymousDreamsSnapshot: any = await (lastSnapshot ? anonymousDreamsRef.startAfter(lastSnapshot.docs[lastSnapshot.docs.length - 1]).limit(500).get() : anonymousDreamsRef.limit(500).get());
            const batch = firestore.batch();

            anonymousDreamsSnapshot.forEach((doc: any) => {
                const newDreamRef = permanentDreamsRef.doc(doc.id);
                batch.set(
                    newDreamRef,
                    rekeyDreamForTransfer(doc.data(), anonymousUid, permanentUid)
                );
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
        hasCompletedOnboarding: false,
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
