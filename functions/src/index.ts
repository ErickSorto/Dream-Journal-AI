/* eslint-disable eol-last */
import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import * as crypto from "crypto";
import express from "express";
import cors from "cors";
import axios from "axios";
import { SecretManagerServiceClient } from "@google-cloud/secret-manager";
import { GoogleGenAI } from "@google/genai";

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
const GEMINI_FAST_MODEL = "gemini-3.5-flash";
let genAI: GoogleGenAI | null = null;
async function getGenAIClient(): Promise<GoogleGenAI> {
    if (genAI === null) {
        const apiKey = await getSecret("GEMENI_SECRET_KEY");
        genAI = new GoogleGenAI({ apiKey });
    }
    return genAI;
}

function getInteractionText(interaction: any): string {
    if (!interaction) {
        return "";
    }

    if (typeof interaction.output_text === "string") {
        return interaction.output_text;
    }
    if (typeof interaction.outputText === "string") {
        return interaction.outputText;
    }
    if (typeof interaction.text === "string") {
        return interaction.text;
    }
    if (typeof interaction.output === "string") {
        return interaction.output;
    }

    return "";
}

type DreamCategorizationResult = {
    isLucid: boolean;
    isNightmare: boolean;
    isRecurring: boolean;
    isFalseAwakening: boolean;
    lucidity: number;
    vividness: number;
    mood: number;
    emotionalRadar: DreamEmotionRadarPayload;
};

type DreamEmotionRadarPayload = {
    joy: number;
    trust: number;
    fear: number;
    surprise: number;
    sadness: number;
    disgust: number;
    anger: number;
    anticipation: number;
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
const OPENAI_RESPONSES_ENDPOINT = "https://api.openai.com/v1/responses";
const OPENAI_IMAGE_ENDPOINT = "https://api.openai.com/v1/images/generations";
const DAILY_LESSON_COLLECTION = "daily_lessons";
const DEBUG_DAILY_LESSON_COLLECTION = "debug_daily_lessons";
const DEBUG_DAILY_LESSON_REQUEST_COLLECTION = "debug_daily_lesson_requests";
const DAILY_LESSON_GENERATION_RUN_COLLECTION = "lesson_generation_runs";
const DAILY_LESSON_REGENERATION_JOB_COLLECTION = "daily_lesson_regeneration_jobs";
const DAILY_LESSON_STORAGE_PREFIX = "daily_lessons";
const DAILY_LESSON_FREE_PER_WEEK = 3;
const DAILY_LESSON_PREMIUM_PER_WEEK = 4;
const DAILY_LESSON_RECENT_HISTORY_LIMIT = 120;
const DAILY_LESSON_TARGET_READ_MINUTES = 5;
const DAILY_LESSON_FREE_ACCESS = "free";
const DAILY_LESSON_PREMIUM_ACCESS = "premium";
const DAILY_LESSON_ADMIN_EMAILS = new Set(["ninjaballista3@gmail.com"]);
const DAILY_LESSON_REGENERATE_CONTENT = "content";
const DAILY_LESSON_REGENERATE_IMAGE = "image";
const DAILY_LESSON_REGENERATION_JOB_TYPE_IMAGE = "lesson_image";
const DREAM_TOKEN_PRODUCTS: Record<string, number> = {
    dream_token_100: 100,
    dream_tokens_100: 100,
    dream_tokens_500: 500,
};

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

type DailyLessonRegenerationJob = {
    id?: string;
    type?: string;
    status?: string;
    lessonId?: string;
    instructions?: string;
    adminIdentity?: string;
    imagePrompt?: string;
    imageUrl?: string;
    errorMessage?: string;
    createdAt?: FirebaseFirestore.Timestamp;
    updatedAt?: FirebaseFirestore.Timestamp;
    startedAt?: FirebaseFirestore.Timestamp;
    completedAt?: FirebaseFirestore.Timestamp;
    failedAt?: FirebaseFirestore.Timestamp;
};

type OpenAIImagePayload = {
    imageBytes: Buffer;
    contentType: string;
};

type DailyLessonAccess = typeof DAILY_LESSON_FREE_ACCESS | typeof DAILY_LESSON_PREMIUM_ACCESS;

type DailyLessonResearchSource = {
    title: string;
    url: string;
    publishedDate?: string;
    sourceType?: string;
    summary?: string;
};

type DailyLessonQuizOption = {
    id: string;
    text: string;
};

type DailyLessonQuizQuestion = {
    id: string;
    prompt: string;
    options: DailyLessonQuizOption[];
    correctOptionId: string;
    explanation: string;
};

type DailyLessonDraft = {
    topic: string;
    title: string;
    quickDescription: string;
    category: string;
    summary: string;
    minutesToRead: number;
    whatYoullLearn: string[];
    quote: string;
    contentMarkdown: string;
    imagePrompt: string;
    questions: DailyLessonQuizQuestion[];
    researchSources: DailyLessonResearchSource[];
};

type DailyLessonDocument = DailyLessonDraft & {
    id: string;
    access: DailyLessonAccess;
    isPremium: boolean;
    isDebug: boolean;
    ownerUid?: string;
    dreamTokenAward: number;
    createdDateIso: string;
    createdAt: FirebaseFirestore.FieldValue;
    updatedAt: FirebaseFirestore.FieldValue;
    weekKey: string;
    weekStartsOn: string;
    dayIndexInWeek: number;
    imageUrl: string;
    imageStoragePath: string;
    generationModel: string;
    imageModelHint: string;
    completed: false;
    started: false;
    bookmarked: false;
    status: "published";
};

type GenerateDailyLessonDocumentOptions = {
    documentId?: string;
    targetRef?: FirebaseFirestore.DocumentReference;
    isDebug?: boolean;
    ownerUid?: string;
    skipExistingCheck?: boolean;
};

type DailyLessonRegenerateSection =
    typeof DAILY_LESSON_REGENERATE_CONTENT |
    typeof DAILY_LESSON_REGENERATE_IMAGE;

type PriorLessonSummary = {
    id: string;
    title: string;
    topic: string;
    category: string;
    summary: string;
};

type DailyLessonCompletionResponse = {
    lessonId: string;
    completed: boolean;
    tokensAwarded: number;
    totalTokens: number;
    quizScore: number;
};

const DAILY_LESSON_CATEGORIES = [
    {
        id: "lucid-dreaming",
        title: "Lucid Dreaming",
        angles: [
            "reality testing",
            "prospective memory",
            "wake-back-to-bed",
            "stabilizing lucidity",
            "ethics and expectations",
        ],
    },
    {
        id: "dream-recall",
        title: "Dream Recall",
        angles: [
            "morning journaling",
            "sleep inertia",
            "memory cues",
            "recall-friendly routines",
            "tracking patterns over time",
        ],
    },
    {
        id: "rem-and-sleep-science",
        title: "REM & Sleep Science",
        angles: [
            "REM physiology",
            "NREM dreams",
            "memory consolidation",
            "circadian timing",
            "sleep architecture",
        ],
    },
    {
        id: "nightmares",
        title: "Nightmares",
        angles: [
            "imagery rehearsal",
            "stress dreams",
            "post-nightmare grounding",
            "recurring dream loops",
            "nightmare frequency",
        ],
    },
    {
        id: "dream-emotions",
        title: "Dream Emotions",
        angles: [
            "emotional regulation",
            "mood and dream tone",
            "fear extinction",
            "social dreams",
            "waking reflection",
        ],
    },
    {
        id: "dream-symbols",
        title: "Dream Symbols",
        angles: [
            "personal symbolism",
            "common motifs",
            "metaphor and memory",
            "symbol clustering",
            "meaning without overclaiming",
        ],
    },
    {
        id: "creativity",
        title: "Creativity & Problem Solving",
        angles: [
            "creative incubation",
            "insight after sleep",
            "visual imagination",
            "practice and performance",
            "dream-inspired ideas",
        ],
    },
    {
        id: "sleep-health",
        title: "Sleep Health",
        angles: [
            "sleep consistency",
            "light exposure",
            "bedtime wind-down",
            "sleep fragmentation",
            "dream-friendly habits",
        ],
    },
    {
        id: "false-awakenings",
        title: "False Awakenings",
        angles: [
            "recognizing loops",
            "morning routines",
            "reality checks",
            "lucid entry points",
            "calm orientation",
        ],
    },
    {
        id: "sleep-paralysis",
        title: "Sleep Paralysis",
        angles: [
            "REM atonia",
            "hypnagogic imagery",
            "calming scripts",
            "risk factors",
            "when to seek care",
        ],
    },
    {
        id: "dream-incubation",
        title: "Dream Incubation",
        angles: [
            "setting an intention",
            "pre-sleep questions",
            "gentle visualization",
            "creative prompts",
            "journaling the answer",
        ],
    },
    {
        id: "dream-technology",
        title: "Dream Technology",
        angles: [
            "wearable sleep cues",
            "lucidity signals",
            "sleep tracking limits",
            "AI dream journaling",
            "research tools",
        ],
    },
];

function currentUtcDay(): string {
    return new Date().toISOString().slice(0, 10);
}

function previousUtcDay(day: string): string {
    const date = new Date(`${day}T00:00:00.000Z`);
    date.setUTCDate(date.getUTCDate() - 1);
    return date.toISOString().slice(0, 10);
}

function addUtcDays(day: string, amount: number): string {
    const date = new Date(`${day}T00:00:00.000Z`);
    date.setUTCDate(date.getUTCDate() + amount);
    return date.toISOString().slice(0, 10);
}

function isoWeekStart(day: string): string {
    const date = new Date(`${day}T00:00:00.000Z`);
    const dayOfWeek = date.getUTCDay();
    const mondayOffset = dayOfWeek === 0 ? -6 : 1 - dayOfWeek;
    date.setUTCDate(date.getUTCDate() + mondayOffset);
    return date.toISOString().slice(0, 10);
}

function dayIndexInIsoWeek(day: string): number {
    const start = new Date(`${isoWeekStart(day)}T00:00:00.000Z`).getTime();
    const current = new Date(`${day}T00:00:00.000Z`).getTime();
    return Math.max(0, Math.min(6, Math.round((current - start) / 86400000)));
}

function seededFloat(seed: string): number {
    const hash = crypto.createHash("sha256").update(seed).digest();
    return hash.readUInt32BE(0) / 0xffffffff;
}

function seededPick<T>(items: T[], seed: string): T {
    if (items.length === 0) {
        throw new Error("Cannot pick from an empty list.");
    }
    const index = Math.floor(seededFloat(seed) * items.length) % items.length;
    return items[index];
}

function normalizeTokenSet(value: string): Set<string> {
    return new Set(
        value
            .toLowerCase()
            .replace(/[^a-z0-9\s-]/g, " ")
            .split(/\s+/)
            .map((word) => word.trim())
            .filter((word) => word.length >= 4)
    );
}

function tokenSimilarity(left: string, right: string): number {
    const a = normalizeTokenSet(left);
    const b = normalizeTokenSet(right);
    if (a.size === 0 || b.size === 0) {
        return 0;
    }

    let overlap = 0;
    a.forEach((token) => {
        if (b.has(token)) {
            overlap += 1;
        }
    });

    return overlap / Math.min(a.size, b.size);
}

function lessonSimilarity(
    draft: Pick<DailyLessonDraft, "title" | "topic" | "summary" | "whatYoullLearn">,
    prior: PriorLessonSummary
): number {
    const left = [
        draft.title,
        draft.topic,
        draft.summary,
        ...(draft.whatYoullLearn || []),
    ].join(" ");
    const right = [
        prior.title,
        prior.topic,
        prior.summary,
    ].join(" ");
    return tokenSimilarity(left, right);
}

function sanitizeLessonSlug(value: string): string {
    const slug = value
        .toLowerCase()
        .replace(/[^a-z0-9]+/g, "-")
        .replace(/^-+|-+$/g, "")
        .slice(0, 80);
    return slug || "daily-lesson";
}

function extractJsonObject(rawText: string): string {
    const normalized = rawText
        .trim()
        .replace(/^```json\s*/i, "")
        .replace(/^```\s*/i, "")
        .replace(/\s*```$/, "");
    const firstBrace = normalized.indexOf("{");
    const lastBrace = normalized.lastIndexOf("}");
    if (firstBrace >= 0 && lastBrace > firstBrace) {
        return normalized.slice(firstBrace, lastBrace + 1);
    }
    return normalized;
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

function readStringArray(value: unknown): string[] {
    if (!Array.isArray(value)) {
        return [];
    }

    return Array.from(new Set(
        value
            .filter((item) => typeof item === "string")
            .map((item) => item.trim())
            .filter((item) => item.length > 0)
    ));
}

function readRecord(value: unknown): Record<string, unknown> {
    if (value && typeof value === "object" && !Array.isArray(value)) {
        return value as Record<string, unknown>;
    }
    return {};
}

function hasRecordEntries(value: unknown): boolean {
    return Object.keys(readRecord(value)).length > 0;
}

function maybeSetNumber(
    patch: FirebaseFirestore.DocumentData,
    field: string,
    value: unknown,
    fallback?: number
): void {
    const parsed = readNumber(value, fallback);
    if (Number.isFinite(parsed) && parsed > 0) {
        patch[field] = parsed;
    }
}

function mergeLessonProgressForAccountLinking(
    anonymousProgress: FirebaseFirestore.DocumentData,
    permanentProgress: FirebaseFirestore.DocumentData,
    lessonId: string
): FirebaseFirestore.DocumentData | null {
    const patch: FirebaseFirestore.DocumentData = {
        lessonId,
    };
    let hasChanges = false;
    const anonymousCompleted = anonymousProgress.completed === true;
    const permanentCompleted = permanentProgress.completed === true;

    if (anonymousProgress.started === true && permanentProgress.started !== true) {
        patch.started = true;
        hasChanges = true;
    }

    if (anonymousProgress.bookmarked === true && permanentProgress.bookmarked !== true) {
        patch.bookmarked = true;
        hasChanges = true;
    }

    if (anonymousCompleted && !permanentCompleted) {
        patch.started = true;
        patch.completed = true;
        patch.updatedAt = admin.firestore.FieldValue.serverTimestamp();
        hasChanges = true;

        if (hasRecordEntries(anonymousProgress.selectedAnswers)) {
            patch.selectedAnswers = readRecord(anonymousProgress.selectedAnswers);
        }
        maybeSetNumber(patch, "quizScore", anonymousProgress.quizScore);
        maybeSetNumber(patch, "questionCount", anonymousProgress.questionCount);
        maybeSetNumber(patch, "tokensAwarded", anonymousProgress.tokensAwarded);
        maybeSetNumber(patch, "completedAtMillis", anonymousProgress.completedAtMillis);
        maybeSetNumber(
            patch,
            "updatedAtMillis",
            anonymousProgress.updatedAtMillis,
            readNumber(anonymousProgress.completedAtMillis, 0)
        );

        if (anonymousProgress.completedAt !== undefined) {
            patch.completedAt = anonymousProgress.completedAt;
        } else {
            patch.completedAt = admin.firestore.FieldValue.serverTimestamp();
        }
    } else if (!permanentCompleted) {
        if (
            !hasRecordEntries(permanentProgress.selectedAnswers) &&
            hasRecordEntries(anonymousProgress.selectedAnswers)
        ) {
            patch.selectedAnswers = readRecord(anonymousProgress.selectedAnswers);
            hasChanges = true;
        }

        const anonymousUpdatedAtMillis = readNumber(anonymousProgress.updatedAtMillis, 0);
        const permanentUpdatedAtMillis = readNumber(permanentProgress.updatedAtMillis, 0);
        if (anonymousUpdatedAtMillis > permanentUpdatedAtMillis) {
            patch.updatedAtMillis = anonymousUpdatedAtMillis;
            patch.updatedAt = admin.firestore.FieldValue.serverTimestamp();
            hasChanges = true;
        }
    }

    return hasChanges ? patch : null;
}

async function transferLessonProgressForAccountLinking(
    anonymousUid: string,
    permanentUid: string
): Promise<{ lessonProgressTransferred: number; lessonCompletionsTransferred: number }> {
    const anonymousProgressRef = firestore
        .collection("users")
        .doc(anonymousUid)
        .collection("lesson_progress");
    const permanentProgressRef = firestore
        .collection("users")
        .doc(permanentUid)
        .collection("lesson_progress");
    let lastSnapshot = null;
    let lessonProgressTransferred = 0;
    let lessonCompletionsTransferred = 0;

    do {
        const anonymousProgressSnapshot: any = await (
            lastSnapshot ?
                anonymousProgressRef
                    .startAfter(lastSnapshot.docs[lastSnapshot.docs.length - 1])
                    .limit(450)
                    .get() :
                anonymousProgressRef.limit(450).get()
        );

        if (anonymousProgressSnapshot.empty) {
            break;
        }

        const permanentProgressSnapshots = await Promise.all(
            anonymousProgressSnapshot.docs.map((doc: any) => permanentProgressRef.doc(doc.id).get())
        );
        const batch = firestore.batch();
        let writes = 0;

        anonymousProgressSnapshot.docs.forEach((doc: any, index: number) => {
            const targetSnapshot = permanentProgressSnapshots[index];
            const patch = mergeLessonProgressForAccountLinking(
                doc.data() || {},
                targetSnapshot.data() || {},
                doc.id
            );
            if (!patch) {
                return;
            }

            batch.set(permanentProgressRef.doc(doc.id), patch, { merge: true });
            writes += 1;
            if (patch.completed === true) {
                lessonCompletionsTransferred += 1;
            }
        });

        if (writes > 0) {
            await batch.commit();
            lessonProgressTransferred += writes;
        }

        lastSnapshot = anonymousProgressSnapshot;
    } while (lastSnapshot && lastSnapshot.size === 450);

    return {
        lessonProgressTransferred,
        lessonCompletionsTransferred,
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

function readProviderErrorField(error: unknown, field: string): string {
    if (!axios.isAxiosError(error)) {
        return "";
    }

    const data = error.response?.data as Record<string, unknown> | undefined;
    const providerError = data?.error;
    if (providerError && typeof providerError === "object") {
        const value = (providerError as Record<string, unknown>)[field];
        return typeof value === "string" ? value : "";
    }

    const value = data?.[field];
    return typeof value === "string" ? value : "";
}

function providerErrorMessage(error: unknown, fallback: string): string {
    const status = axios.isAxiosError(error) ? error.response?.status ?? error.status : undefined;
    const providerMessage = readProviderErrorField(error, "message");
    const providerCode = readProviderErrorField(error, "code");
    const rawMessage = providerMessage ||
        (error instanceof Error ? error.message : "") ||
        fallback;
    const normalized = `${status || ""} ${providerCode} ${rawMessage}`.toLowerCase();

    if (
        status === 429 ||
        normalized.includes("insufficient_quota") ||
        normalized.includes("quota") ||
        normalized.includes("billing") ||
        normalized.includes("credit") ||
        normalized.includes("rate limit")
    ) {
        return "AI service quota or rate limit was reached.";
    }

    if (
        status === 401 ||
        status === 403 ||
        normalized.includes("api key") ||
        normalized.includes("unauthorized")
    ) {
        return "AI service credentials are not configured correctly.";
    }

    if (
        normalized.includes("content") ||
        normalized.includes("safety") ||
        normalized.includes("policy")
    ) {
        return "The request was blocked by the AI safety system.";
    }

    if (
        normalized.includes("timeout") ||
        normalized.includes("deadline")
    ) {
        return "AI service timed out.";
    }

    return rawMessage.slice(0, 500);
}

function safeErrorLog(error: unknown): Record<string, unknown> {
    if (axios.isAxiosError(error)) {
        return {
            name: error.name,
            message: providerErrorMessage(error, "Provider request failed."),
            status: error.response?.status ?? error.status,
            code: error.code,
            providerCode: readProviderErrorField(error, "code"),
            providerType: readProviderErrorField(error, "type"),
        };
    }

    if (error instanceof Error) {
        return {
            name: error.name,
            message: error.message,
        };
    }

    return { message: String(error) };
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

    let response;
    try {
        response = await axios.post(
            OPENAI_CHAT_ENDPOINT,
            body,
            {
                headers: { Authorization: `Bearer ${apiKey}` },
                timeout: 120000,
            }
        );
    } catch (error) {
        throw new Error(providerErrorMessage(error, "OpenAI chat request failed."));
    }

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

    const model = cost <= 1 ? "gpt-5.4-mini" : "gpt-5.5";
    const reasoning = cost <= 1 ? "low" : "high";
    try {
        return await openAIChat(prompt, 2000, model, reasoning);
    } catch (error) {
        if (cost > 1) {
            functions.logger.warn("Premium dream image prompt model failed; trying fallback.", {
                model,
                fallbackModel: "gpt-5.4-mini",
                error: safeErrorLog(error),
            });
            return openAIChat(prompt, 2000, "gpt-5.4-mini", "medium");
        }
        throw error;
    }
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

async function generateOpenAIImage(details: string, style: string, _cost: number): Promise<OpenAIImagePayload> {
    const apiKey = await getOpenAISecretKey();
    const beautyDirection = "beautiful dream artwork, luminous balanced exposure, clear readable details, elegant composition, layered depth, soft atmospheric glow, vivid natural color accents, gentle highlights, avoid dull muddy palettes, avoid crushed blacks, avoid overly dark or underexposed lighting unless explicitly required by the dream";
    const finalPrompt = `${sanitizePromptText(details, "A beautiful, peaceful dream scene")}, ${style || "dreamlike cinematic illustration"}, ${beautyDirection}`;
    const primaryModel = "gpt-image-2";
    const fallbackModel = "gpt-image-1";

    async function requestImage(model: string): Promise<OpenAIImagePayload> {
        const normalizedModel = model.toLowerCase();
        let response;
        try {
            response = await axios.post(
                OPENAI_IMAGE_ENDPOINT,
                {
                    model: normalizedModel,
                    prompt: finalPrompt,
                    size: "1024x1024",
                    quality: normalizedModel === "gpt-image-1-mini" ? "low" : "high",
                    n: 1,
                },
                {
                    headers: { Authorization: `Bearer ${apiKey}` },
                    timeout: 180000,
                }
            );
        } catch (error) {
            throw new Error(providerErrorMessage(error, "OpenAI image request failed."));
        }

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
        functions.logger.warn("Primary image model failed; trying fallback.", {
            primaryModel,
            fallbackModel,
            error: safeErrorLog(error),
        });
        return requestImage(fallbackModel);
    }
}

function extractOpenAIResponseText(responseData: unknown): string {
    const data = responseData as Record<string, unknown>;
    const outputText = data.output_text;
    if (typeof outputText === "string" && outputText.trim() !== "") {
        return outputText.trim();
    }

    const output = Array.isArray(data.output) ? data.output : [];
    for (const item of output) {
        if (!item || typeof item !== "object") {
            continue;
        }
        const content = (item as Record<string, unknown>).content;
        if (!Array.isArray(content)) {
            continue;
        }
        for (const part of content) {
            if (!part || typeof part !== "object") {
                continue;
            }
            const text = (part as Record<string, unknown>).text;
            if (typeof text === "string" && text.trim() !== "") {
                return text.trim();
            }
        }
    }

    throw new Error("OpenAI returned an empty Responses API output.");
}

function extractOpenAIWebSources(responseData: unknown): DailyLessonResearchSource[] {
    const data = responseData as Record<string, unknown>;
    const seen = new Set<string>();
    const sources: DailyLessonResearchSource[] = [];

    function addSource(url: unknown, title: unknown, summary?: unknown): void {
        if (typeof url !== "string" || url.trim() === "") {
            return;
        }
        const normalizedUrl = url.trim();
        if (seen.has(normalizedUrl)) {
            return;
        }
        seen.add(normalizedUrl);
        sources.push({
            url: normalizedUrl,
            title: typeof title === "string" && title.trim() !== "" ?
                title.trim().slice(0, 180) :
                normalizedUrl,
            summary: typeof summary === "string" ? summary.trim().slice(0, 260) : undefined,
            sourceType: "web",
        });
    }

    const output = Array.isArray(data.output) ? data.output : [];
    output.forEach((item) => {
        if (!item || typeof item !== "object") {
            return;
        }
        const itemRecord = item as Record<string, unknown>;
        const action = itemRecord.action;
        if (action && typeof action === "object") {
            const actionRecord = action as Record<string, unknown>;
            const actionSources = Array.isArray(actionRecord.sources) ? actionRecord.sources : [];
            actionSources.forEach((source) => {
                if (source && typeof source === "object") {
                    const sourceRecord = source as Record<string, unknown>;
                    addSource(sourceRecord.url, sourceRecord.title, sourceRecord.snippet);
                }
            });
        }

        const content = Array.isArray(itemRecord.content) ? itemRecord.content : [];
        content.forEach((part) => {
            if (!part || typeof part !== "object") {
                return;
            }
            const annotations = (part as Record<string, unknown>).annotations;
            if (!Array.isArray(annotations)) {
                return;
            }
            annotations.forEach((annotation) => {
                if (annotation && typeof annotation === "object") {
                    const annotationRecord = annotation as Record<string, unknown>;
                    if (annotationRecord.type === "url_citation") {
                        addSource(annotationRecord.url, annotationRecord.title);
                    }
                }
            });
        });
    });

    return sources.slice(0, 8);
}

function lessonJsonSchema(): Record<string, unknown> {
    const stringArray = {
        type: "array",
        minItems: 4,
        maxItems: 4,
        items: { type: "string" },
    };
    const sourceSchema = {
        type: "object",
        additionalProperties: false,
        required: ["title", "url", "publishedDate", "sourceType", "summary"],
        properties: {
            title: { type: "string" },
            url: { type: "string" },
            publishedDate: { type: "string" },
            sourceType: { type: "string" },
            summary: { type: "string" },
        },
    };
    const optionSchema = {
        type: "object",
        additionalProperties: false,
        required: ["id", "text"],
        properties: {
            id: { type: "string", enum: ["a", "b", "c", "d"] },
            text: { type: "string" },
        },
    };
    const questionSchema = {
        type: "object",
        additionalProperties: false,
        required: ["id", "prompt", "options", "correctOptionId", "explanation"],
        properties: {
            id: { type: "string" },
            prompt: { type: "string" },
            options: {
                type: "array",
                minItems: 4,
                maxItems: 4,
                items: optionSchema,
            },
            correctOptionId: { type: "string", enum: ["a", "b", "c", "d"] },
            explanation: { type: "string" },
        },
    };

    return {
        type: "object",
        additionalProperties: false,
        required: [
            "topic",
            "title",
            "quickDescription",
            "category",
            "summary",
            "minutesToRead",
            "whatYoullLearn",
            "quote",
            "contentMarkdown",
            "imagePrompt",
            "questions",
            "researchSources",
        ],
        properties: {
            topic: { type: "string" },
            title: { type: "string" },
            quickDescription: { type: "string" },
            category: { type: "string" },
            summary: { type: "string" },
            minutesToRead: { type: "integer", minimum: 3, maximum: DAILY_LESSON_TARGET_READ_MINUTES },
            whatYoullLearn: stringArray,
            quote: { type: "string" },
            contentMarkdown: { type: "string" },
            imagePrompt: { type: "string" },
            questions: {
                type: "array",
                minItems: 3,
                maxItems: 3,
                items: questionSchema,
            },
            researchSources: {
                type: "array",
                minItems: 2,
                maxItems: 5,
                items: sourceSchema,
            },
        },
    };
}

async function chooseDailyLessonAccess(day: string): Promise<DailyLessonAccess> {
    const weekStartsOn = isoWeekStart(day);
    const weekEndsOn = addUtcDays(weekStartsOn, 6);
    const weekSnap = await firestore
        .collection(DAILY_LESSON_COLLECTION)
        .where("createdDateIso", ">=", weekStartsOn)
        .where("createdDateIso", "<=", weekEndsOn)
        .get();
    let premiumCount = 0;
    let freeCount = 0;
    weekSnap.docs.forEach((doc) => {
        const access = readString(doc.data().access);
        if (access === DAILY_LESSON_PREMIUM_ACCESS) {
            premiumCount += 1;
        } else if (access === DAILY_LESSON_FREE_ACCESS) {
            freeCount += 1;
        }
    });

    const premiumRemaining = Math.max(0, DAILY_LESSON_PREMIUM_PER_WEEK - premiumCount);
    const freeRemaining = Math.max(0, DAILY_LESSON_FREE_PER_WEEK - freeCount);
    if (premiumRemaining <= 0) {
        return DAILY_LESSON_FREE_ACCESS;
    }
    if (freeRemaining <= 0) {
        return DAILY_LESSON_PREMIUM_ACCESS;
    }

    const slots: DailyLessonAccess[] = [
        ...Array(premiumRemaining).fill(DAILY_LESSON_PREMIUM_ACCESS),
        ...Array(freeRemaining).fill(DAILY_LESSON_FREE_ACCESS),
    ];
    return seededPick(slots, `lesson-access:${day}:${premiumCount}:${freeCount}`);
}

async function getPriorLessonSummaries(): Promise<PriorLessonSummary[]> {
    const snap = await firestore
        .collection(DAILY_LESSON_COLLECTION)
        .orderBy("createdDateIso", "desc")
        .limit(DAILY_LESSON_RECENT_HISTORY_LIMIT)
        .get();
    return snap.docs.map((doc) => {
        const data = doc.data();
        return {
            id: doc.id,
            title: readString(data.title),
            topic: readString(data.topic),
            category: readString(data.category),
            summary: readString(data.summary),
        };
    });
}

function buildDailyLessonPrompt(
    day: string,
    access: DailyLessonAccess,
    category: typeof DAILY_LESSON_CATEGORIES[number],
    angle: string,
    priorLessons: PriorLessonSummary[],
    rejectedTopics: string[]
): string {
    const isPremium = access === DAILY_LESSON_PREMIUM_ACCESS;
    const priorSummary = priorLessons
        .slice(0, 80)
        .map((lesson, index) => `${index + 1}. ${lesson.category} | ${lesson.title} | ${lesson.topic} | ${lesson.summary}`)
        .join("\n");
    const rejected = rejectedTopics.length > 0 ?
        `\nRejected draft notes today: ${rejectedTopics.join("; ")}\nChoose a meaningfully different concept and fix those writing issues.` :
        "";

    return `
You are DreamNorth's daily lesson research agent and curriculum writer.

Create one mobile-friendly dream education lesson for ${day}.

Access tier: ${access}.
Category chosen by the lesson pool: ${category.title}.
Suggested fresh angle: ${angle}.
The lesson must be ${isPremium ? "a little richer, more beautiful, and more practical" : "short, clear, and approachable"}.

Research requirement:
- Search the web before writing.
- Prefer current or authoritative dream/sleep science sources such as PubMed, NIH/PMC, AASM, university sleep labs, peer-reviewed journals, and reputable sleep research explainers.
- Use careful language. Do not overclaim benefits. Avoid medical advice.
- Store 2-5 cited sources in researchSources.
- In contentMarkdown, include 2-4 light inline source citations where research claims appear.
- Format each inline citation with the source domain as the visible text, exactly like this: ([domain.com](https://domain.com/path)).
- Remove "www." from the visible domain when present. Do not use generic link text like "link" or "source".
- Use only URLs you also include in researchSources. Do not add raw standalone URLs.

Duplicate avoidance:
Do not repeat an existing lesson concept. A same category is allowed only if this lesson introduces a clearly new mechanism, study finding, practice, or angle.
Recent lesson summaries:
${priorSummary || "No prior lessons yet."}
${rejected}

Content constraints:
- Reading time must be 3-${DAILY_LESSON_TARGET_READ_MINUTES} minutes.
- Write for a phone screen: short paragraphs, useful headings, warm but not childish.
- Use plain, layman-friendly language. Briefly explain any scientific term in the sentence where it appears.
- Keep the tone calm, polished, practical, and encouraging. Do not sound academic, salesy, mystical, or childish.
- Avoid decorative punctuation. Outside the required inline citations, use parentheses only when truly needed. Avoid em dashes and avoid colon-heavy sentences.
- Do not use a colon in any markdown heading.
- Markdown content must stay simple: 3-5 ## headings, short paragraphs, and only occasional one-level lists.
- A section may use either one short bullet list or one short numbered list, but not both.
- Use "- " for bullets or "1. ", "2. " for numbered steps. Do not use nested lists, mixed bullet/numbered lists in the same section, bold labels, or "term: explanation" list items.
- Each list item must be one clean sentence. Do not put a heading inside a list item.
- Prefer natural paragraph transitions over lots of lists.
- Avoid huge blocks of text. Keep most paragraphs to 1-3 sentences.
- Keep inline citations in normal paragraphs, not in headings, bullets, quizzes, titles, or quick descriptions.
- Use no more than one inline citation in the same paragraph.
- Use quotes only in the quote field, not inside contentMarkdown, unless you are clearly quoting a cited source.
- Include exactly 4 "whatYoullLearn" sentences.
- whatYoullLearn items should be friendly complete sentences without numbering or label prefixes.
- Titles and quick descriptions should avoid parentheses, colons, and dash-heavy phrasing.
- Include exactly 3 multiple-choice questions, each with options a/b/c/d, one correct answer, and a short explanation.
- Quiz prompts and explanations should be easy to understand. Avoid trick wording and heavy jargon.
- Option text should not include "A.", "B.", numbers, bullets, or label prefixes because the app adds those.
- The quote should be one graceful, layman-friendly sentence. Use an exact source quote only when it is clearly cited in researchSources.
- The dreamTokenAward is handled by the server; do not include it.
- Generate an imagePrompt for a beautiful square lesson artwork. Avoid text, logos, UI, or dark muddy lighting.

Return only the JSON object matching the schema.
`.trim();
}

function countRegexMatches(value: string, pattern: RegExp): number {
    return value.match(pattern)?.length || 0;
}

function stripMarkdownLinksForFormattingReview(value: string): string {
    return value
        .replace(/\(\[[^\]]+\]\([^)]+\)\)/g, "")
        .replace(/\[[^\]]+\]\([^)]+\)/g, "");
}

function cleanGeneratedListText(value: string): string {
    return value
        .replace(/^\s*(?:[-*]\s+|\d+[\.)]\s+|[a-dA-D][\.)]\s+)/, "")
        .trim();
}

async function requireDailyLessonAdmin(context: functions.https.CallableContext): Promise<string> {
    if (!context.auth) {
        throw new functions.https.HttpsError("unauthenticated", "Please sign in before editing lessons.");
    }

    const email = readString(context.auth.token.email).trim().toLowerCase();
    const isCustomClaimAdmin = context.auth.token.admin === true ||
        context.auth.token.lessonAdmin === true;
    if (isCustomClaimAdmin || DAILY_LESSON_ADMIN_EMAILS.has(email)) {
        return email || context.auth.uid;
    }

    const uid = readString(context.auth.uid).trim();
    if (uid.length > 0) {
        const userSnap = await firestore.collection("users").doc(uid).get();
        const userEmail = readString(userSnap.data()?.email).trim().toLowerCase();
        const hasUserDocAdminFlag = userSnap.data()?.admin === true ||
            userSnap.data()?.lessonAdmin === true;
        if (hasUserDocAdminFlag || DAILY_LESSON_ADMIN_EMAILS.has(userEmail)) {
            return userEmail || uid;
        }
    }

    throw new functions.https.HttpsError("permission-denied", "Lesson admin access is required.");
}

function readDailyLessonResearchSources(value: unknown): DailyLessonResearchSource[] {
    if (!Array.isArray(value)) {
        return [];
    }

    return value
        .map((item) => {
            const source = item as Record<string, unknown>;
            return {
                title: readString(source.title).trim().slice(0, 180),
                url: readString(source.url).trim(),
                publishedDate: readString(source.publishedDate).trim().slice(0, 40),
                sourceType: readString(source.sourceType, "research").trim().slice(0, 60),
                summary: readString(source.summary).trim().slice(0, 260),
            };
        })
        .filter((source) => source.title !== "" && source.url.startsWith("http"))
        .slice(0, 6);
}

function reviewDailyLessonFormatting(draft: DailyLessonDraft): string | null {
    const markdown = draft.contentMarkdown.trim();
    const lines = markdown.split(/\r?\n/);
    const contentWithoutBulletMarkers = lines
        .map((line) => line.replace(/^\s*(?:[-*]\s+|\d+\.\s+)/, ""))
        .join("\n");
    const contentForPunctuationReview = stripMarkdownLinksForFormattingReview(contentWithoutBulletMarkers);
    const headingLines = lines.filter((line) => /^#{1,6}\s+/.test(line.trim()));

    if (headingLines.length < 3 || headingLines.length > 5) {
        return "contentMarkdown should use 3 to 5 simple headings";
    }
    if (headingLines.some((line) => line.includes(":"))) {
        return "markdown headings should not contain colons";
    }

    let sectionListKind = "";
    let sectionListBlocks = 0;
    let sectionListItems = 0;
    let previousLineWasList = false;
    for (const line of lines) {
        const trimmed = line.trim();
        if (/^#{1,6}\s+/.test(trimmed)) {
            sectionListKind = "";
            sectionListBlocks = 0;
            sectionListItems = 0;
            previousLineWasList = false;
            continue;
        }
        if (/^\s{2,}(?:[-*]|\d+[\.)])\s+/.test(line)) {
            return "contentMarkdown used a nested list";
        }
        if (/^\*\s+/.test(trimmed)) {
            return "contentMarkdown should use dash bullets instead of star bullets";
        }
        if (/^\d+\)\s+/.test(trimmed)) {
            return "contentMarkdown numbered lists should use 1. style";
        }

        const listKind = /^-\s+/.test(trimmed) ? "bullet" : (/^\d+\.\s+/.test(trimmed) ? "number" : "");
        if (listKind !== "") {
            if (sectionListKind !== "" && sectionListKind !== listKind) {
                return "contentMarkdown mixed bullet and numbered lists in one section";
            }
            if (!previousLineWasList) {
                sectionListBlocks += 1;
            }
            if (sectionListBlocks > 1) {
                return "contentMarkdown used more than one list in a section";
            }
            sectionListKind = listKind;
            sectionListItems += 1;
            previousLineWasList = true;
            if (sectionListItems > 5) {
                return "contentMarkdown used too many list items in one section";
            }
        } else if (trimmed !== "") {
            previousLineWasList = false;
        }
    }

    if (countRegexMatches(contentForPunctuationReview, /[()]/g) > 6) {
        return "contentMarkdown used too many parentheses";
    }
    if (countRegexMatches(contentForPunctuationReview, /[\u2013\u2014]| - /g) > 4) {
        return "contentMarkdown used too many dash breaks";
    }
    if (countRegexMatches(contentForPunctuationReview, /:/g) > 8) {
        return "contentMarkdown used too many colons";
    }
    if (lines.some((line) => {
        const trimmed = line.trim();
        return trimmed !== "" && !trimmed.startsWith("#") && !trimmed.startsWith("-") && trimmed.length > 520;
    })) {
        return "contentMarkdown has a paragraph that is too long for mobile";
    }
    if ([draft.title, draft.quickDescription, draft.summary].some((value) => countRegexMatches(value, /[()]/g) > 2)) {
        return "title, description, or summary used too many parentheses";
    }

    return null;
}

function lessonContentOnlyJsonSchema(): Record<string, unknown> {
    return {
        type: "object",
        additionalProperties: false,
        required: ["contentMarkdown"],
        properties: {
            contentMarkdown: { type: "string" },
        },
    };
}

function lessonDraftForContentReview(
    lesson: Record<string, unknown>,
    contentMarkdown: string
): DailyLessonDraft {
    return {
        topic: readString(lesson.topic).trim(),
        title: readString(lesson.title).trim(),
        quickDescription: readString(lesson.quickDescription).trim(),
        category: readString(lesson.category).trim(),
        summary: readString(lesson.summary).trim(),
        minutesToRead: Math.max(3, Math.min(DAILY_LESSON_TARGET_READ_MINUTES, Math.round(readNumber(lesson.minutesToRead, 4)))),
        whatYoullLearn: Array.isArray(lesson.whatYoullLearn) ?
            lesson.whatYoullLearn.map((item) => readString(item).trim()).filter(Boolean).slice(0, 4) :
            [],
        quote: readString(lesson.quote).trim(),
        contentMarkdown,
        imagePrompt: readString(lesson.imagePrompt).trim(),
        questions: [],
        researchSources: readDailyLessonResearchSources(lesson.researchSources),
    };
}

function buildAdminContentRegenerationPrompt(
    lesson: Record<string, unknown>,
    instructions: string,
    rejectedIssues: string[]
): string {
    const sources = readDailyLessonResearchSources(lesson.researchSources)
        .map((source, index) => `${index + 1}. ${source.title} | ${source.url} | ${source.summary || source.sourceType || "source"}`)
        .join("\n");
    const rejected = rejectedIssues.length > 0 ?
        `\nFormatting issues from rejected drafts:\n${rejectedIssues.map((issue) => `- ${issue}`).join("\n")}` :
        "";

    return `
You are DreamNorth's admin lesson editor.

Rewrite only the contentMarkdown for this already-published lesson.
Do not change the lesson topic, title, quick description, category, quiz, access tier, or reward.

Lesson context:
Title: ${readString(lesson.title)}
Topic: ${readString(lesson.topic)}
Category: ${readString(lesson.category)}
Quick description: ${readString(lesson.quickDescription)}
Summary: ${readString(lesson.summary)}
Access tier: ${readString(lesson.access)}

Current sources:
${sources || "No source list was stored. Search only if needed for accuracy."}

Current contentMarkdown:
${sanitizePromptText(readString(lesson.contentMarkdown), "No current content was stored.")}

Admin request:
${sanitizePromptText(instructions, "Clean up the formatting and make the content easier to read on mobile.")}
${rejected}

Content rules:
- Keep the same lesson concept and factual scope.
- Use plain, layman-friendly language. Briefly explain any scientific term in the sentence where it appears.
- Keep the tone calm, polished, practical, and encouraging.
- Avoid decorative punctuation. Outside inline citations, use parentheses only when truly needed. Avoid em dashes and colon-heavy sentences.
- Preserve or add 2-4 light inline source citations where research claims appear.
- Format each inline citation with the source domain as the visible text, exactly like this: ([domain.com](https://domain.com/path)).
- Remove "www." from the visible domain when present. Do not use generic link text like "link" or "source".
- Use the current sources when possible. Do not add raw standalone URLs.
- Keep inline citations in normal paragraphs, not in headings or bullets.
- Use no more than one inline citation in the same paragraph.
- Use 3-5 ## headings. Do not use a colon in any heading.
- Use short paragraphs. Keep most paragraphs to 1-3 sentences.
- A section may use either one short bullet list or one short numbered list, but not both.
- Use "- " for bullets or "1. ", "2. " for numbered steps. Do not use nested lists, mixed bullet/numbered lists in the same section, bold labels, or "term: explanation" list items.
- Each list item must be one clean sentence.
- Do not include title text, metadata, standalone source URLs, quiz questions, or app instructions inside contentMarkdown.
- Avoid medical advice and do not overclaim research.

Return only JSON with contentMarkdown.
`.trim();
}

async function regenerateDailyLessonContentMarkdown(
    lesson: Record<string, unknown>,
    instructions: string
): Promise<{ contentMarkdown: string; model: string; webSources: DailyLessonResearchSource[] }> {
    const apiKey = await getOpenAISecretKey();
    const model = process.env.LESSON_TEXT_MODEL || "gpt-5.5";
    const rejectedIssues: string[] = [];

    for (let attempt = 0; attempt < 3; attempt += 1) {
        const response = await axios.post(
            OPENAI_RESPONSES_ENDPOINT,
            {
                model,
                reasoning: { effort: "medium" },
                tools: [
                    {
                        type: "web_search",
                        filters: {
                            allowed_domains: [
                                "pubmed.ncbi.nlm.nih.gov",
                                "pmc.ncbi.nlm.nih.gov",
                                "nih.gov",
                                "nature.com",
                                "frontiersin.org",
                                "sciencedirect.com",
                                "tandfonline.com",
                                "aasm.org",
                                "sleepfoundation.org",
                                "neurosciencenews.com",
                                "medicalxpress.com",
                            ],
                        },
                    },
                ],
                tool_choice: "auto",
                include: ["web_search_call.action.sources"],
                max_output_tokens: 3600,
                text: {
                    format: {
                        type: "json_schema",
                        name: "daily_dream_lesson_content_revision",
                        strict: true,
                        schema: lessonContentOnlyJsonSchema(),
                    },
                },
                input: buildAdminContentRegenerationPrompt(lesson, instructions, rejectedIssues),
            },
            {
                headers: { Authorization: `Bearer ${apiKey}` },
                timeout: 180000,
            }
        );

        const rawText = extractOpenAIResponseText(response.data);
        const parsed = JSON.parse(extractJsonObject(rawText)) as Record<string, unknown>;
        const contentMarkdown = readString(parsed.contentMarkdown).trim();
        if (contentMarkdown === "") {
            rejectedIssues.push("contentMarkdown was empty");
            continue;
        }

        const formattingIssue = reviewDailyLessonFormatting(
            lessonDraftForContentReview(lesson, contentMarkdown)
        );
        if (formattingIssue && attempt < 2) {
            rejectedIssues.push(formattingIssue);
            functions.logger.info("Rejected admin lesson content regeneration for formatting.", {
                lessonId: readString(lesson.id),
                formattingIssue,
                attempt: attempt + 1,
            });
            continue;
        }

        return {
            contentMarkdown,
            model,
            webSources: extractOpenAIWebSources(response.data),
        };
    }

    throw new Error("Failed to regenerate lesson content.");
}

async function regenerateDailyLessonImagePrompt(
    lesson: Record<string, unknown>,
    instructions: string
): Promise<string> {
    const prompt = `
Create one polished image prompt for regenerating a square DreamNorth lesson artwork.

Keep the lesson identity the same, but follow the admin request.

Lesson title: ${readString(lesson.title)}
Topic: ${readString(lesson.topic)}
Category: ${readString(lesson.category)}
Summary: ${readString(lesson.summary)}
Current image prompt: ${readString(lesson.imagePrompt)}

Admin request:
${sanitizePromptText(instructions, "Make the image more beautiful, clear, luminous, and directly related to the lesson.")}

Image rules:
- Square editorial artwork for a mobile lesson detail page.
- No text, logos, UI, charts, diagrams, captions, or readable writing.
- Make it beautiful, luminous, calm, and clear.
- Avoid muddy darkness, horror, clutter, or generic stock-photo vibes.
- Return only the final image prompt sentence.
`.trim();

    const rawPrompt = await openAIChat(
        prompt,
        900,
        process.env.LESSON_IMAGE_PROMPT_MODEL || "gpt-5.4-mini",
        "low"
    );

    return rawPrompt
        .replace(/^image prompt:\s*/i, "")
        .replace(/^["']|["']$/g, "")
        .trim()
        .slice(0, 1200);
}

function validateDailyLessonDraft(rawDraft: unknown, access: DailyLessonAccess): DailyLessonDraft {
    if (!rawDraft || typeof rawDraft !== "object" || Array.isArray(rawDraft)) {
        throw new Error("Lesson response was not a JSON object.");
    }
    const payload = rawDraft as Record<string, unknown>;
    const questions = Array.isArray(payload.questions) ? payload.questions : [];
    const sources = Array.isArray(payload.researchSources) ? payload.researchSources : [];
    const whatYoullLearn = Array.isArray(payload.whatYoullLearn) ?
        payload.whatYoullLearn.map((item) => cleanGeneratedListText(readString(item))).filter(Boolean).slice(0, 4) :
        [];
    if (questions.length !== 3) {
        throw new Error("Lesson response did not include exactly three questions.");
    }
    if (whatYoullLearn.length !== 4) {
        throw new Error("Lesson response did not include exactly four learning items.");
    }

    const normalizedQuestions = questions.map((item, index) => {
        const question = item as Record<string, unknown>;
        const options = Array.isArray(question.options) ? question.options : [];
        if (options.length !== 4) {
            throw new Error("Lesson question did not include exactly four options.");
        }
        const normalizedOptions = options.map((option, optionIndex) => {
            const optionRecord = option as Record<string, unknown>;
            const fallbackId = ["a", "b", "c", "d"][optionIndex];
            return {
                id: readString(optionRecord.id, fallbackId).trim().toLowerCase().slice(0, 1) || fallbackId,
                text: cleanGeneratedListText(readString(optionRecord.text)).slice(0, 220),
            };
        });
        const correctOptionId = readString(question.correctOptionId).trim().toLowerCase().slice(0, 1);
        if (!["a", "b", "c", "d"].includes(correctOptionId)) {
            throw new Error("Lesson question had an invalid correct option.");
        }
        return {
            id: readString(question.id, `q${index + 1}`).trim() || `q${index + 1}`,
            prompt: readString(question.prompt).trim().slice(0, 260),
            options: normalizedOptions,
            correctOptionId,
            explanation: readString(question.explanation).trim().slice(0, 300),
        };
    });

    const minutesToRead = Math.max(
        3,
        Math.min(
            DAILY_LESSON_TARGET_READ_MINUTES,
            Math.round(readNumber(payload.minutesToRead, access === DAILY_LESSON_PREMIUM_ACCESS ? 5 : 4))
        )
    );

    const normalizedSources = sources
        .map((item) => {
            const source = item as Record<string, unknown>;
            return {
                title: readString(source.title).trim().slice(0, 180),
                url: readString(source.url).trim(),
                publishedDate: readString(source.publishedDate).trim().slice(0, 40),
                sourceType: readString(source.sourceType, "research").trim().slice(0, 60),
                summary: readString(source.summary).trim().slice(0, 260),
            };
        })
        .filter((source) => source.title !== "" && source.url.startsWith("http"))
        .slice(0, 5);

    return {
        topic: readString(payload.topic).trim().slice(0, 140),
        title: readString(payload.title).trim().slice(0, 90),
        quickDescription: readString(payload.quickDescription).trim().slice(0, 220),
        category: readString(payload.category).trim().slice(0, 80),
        summary: readString(payload.summary).trim().slice(0, 360),
        minutesToRead,
        whatYoullLearn,
        quote: readString(payload.quote).trim().slice(0, 180),
        contentMarkdown: readString(payload.contentMarkdown).trim(),
        imagePrompt: readString(payload.imagePrompt).trim().slice(0, 1200),
        questions: normalizedQuestions,
        researchSources: normalizedSources,
    };
}

async function generateDailyLessonDraft(
    day: string,
    access: DailyLessonAccess,
    priorLessons: PriorLessonSummary[],
    rejectedTopics: string[]
): Promise<{ draft: DailyLessonDraft; model: string; webSources: DailyLessonResearchSource[] }> {
    const category = seededPick(
        DAILY_LESSON_CATEGORIES,
        `lesson-category:${day}:${rejectedTopics.length}`
    );
    const angle = seededPick(
        category.angles,
        `lesson-angle:${day}:${category.id}:${rejectedTopics.length}`
    );
    const prompt = buildDailyLessonPrompt(day, access, category, angle, priorLessons, rejectedTopics);
    const apiKey = await getOpenAISecretKey();
    const model = process.env.LESSON_TEXT_MODEL || "gpt-5.5";
    const response = await axios.post(
        OPENAI_RESPONSES_ENDPOINT,
        {
            model,
            reasoning: { effort: access === DAILY_LESSON_PREMIUM_ACCESS ? "medium" : "low" },
            tools: [
                {
                    type: "web_search",
                    filters: {
                        allowed_domains: [
                            "pubmed.ncbi.nlm.nih.gov",
                            "pmc.ncbi.nlm.nih.gov",
                            "nih.gov",
                            "nature.com",
                            "frontiersin.org",
                            "sciencedirect.com",
                            "tandfonline.com",
                            "aasm.org",
                            "sleepfoundation.org",
                            "neurosciencenews.com",
                            "medicalxpress.com",
                        ],
                    },
                },
            ],
            tool_choice: "auto",
            include: ["web_search_call.action.sources"],
            max_output_tokens: access === DAILY_LESSON_PREMIUM_ACCESS ? 5600 : 4300,
            text: {
                format: {
                    type: "json_schema",
                    name: "daily_dream_lesson",
                    strict: true,
                    schema: lessonJsonSchema(),
                },
            },
            input: prompt,
        },
        {
            headers: { Authorization: `Bearer ${apiKey}` },
            timeout: 180000,
        }
    );

    const rawText = extractOpenAIResponseText(response.data);
    const parsed = JSON.parse(extractJsonObject(rawText));
    const draft = validateDailyLessonDraft(parsed, access);
    const webSources = extractOpenAIWebSources(response.data);
    return { draft, model, webSources };
}

async function generateDailyLessonDocument(
    day: string,
    options: GenerateDailyLessonDocumentOptions = {}
): Promise<DailyLessonDocument> {
    const documentId = options.documentId || day;
    const targetRef = options.targetRef ||
        firestore.collection(DAILY_LESSON_COLLECTION).doc(documentId);
    const isDebug = options.isDebug === true;
    const existingSnap = options.skipExistingCheck === true ? null : await targetRef.get();
    if (existingSnap?.exists) {
        return existingSnap.data() as DailyLessonDocument;
    }

    const debugPremiumThreshold = DAILY_LESSON_PREMIUM_PER_WEEK /
        (DAILY_LESSON_PREMIUM_PER_WEEK + DAILY_LESSON_FREE_PER_WEEK);
    const access = isDebug ?
        (seededFloat(`debug-lesson-access:${documentId}`) < debugPremiumThreshold ?
            DAILY_LESSON_PREMIUM_ACCESS :
            DAILY_LESSON_FREE_ACCESS) :
        await chooseDailyLessonAccess(day);
    const priorLessons = await getPriorLessonSummaries();
    const rejectedTopics: string[] = [];
    let draft: DailyLessonDraft | null = null;
    let model = "";
    let webSources: DailyLessonResearchSource[] = [];

    for (let attempt = 0; attempt < 3; attempt += 1) {
        const result = await generateDailyLessonDraft(day, access, priorLessons, rejectedTopics);
        const formattingIssue = reviewDailyLessonFormatting(result.draft);
        if (formattingIssue && attempt < 2) {
            rejectedTopics.push(formattingIssue);
            functions.logger.info("Rejected daily lesson draft for formatting.", {
                day,
                access,
                formattingIssue,
                attempt: attempt + 1,
            });
            continue;
        }

        const closestPrior = priorLessons
            .map((prior) => ({
                prior,
                score: lessonSimilarity(result.draft, prior),
            }))
            .sort((a, b) => b.score - a.score)[0];

        if (!closestPrior || closestPrior.score < 0.62 || attempt === 2) {
            draft = result.draft;
            model = result.model;
            webSources = result.webSources;
            break;
        }

        rejectedTopics.push(`${result.draft.topic} looked too close to ${closestPrior.prior.title}`);
    }

    if (!draft) {
        throw new Error("Failed to generate a lesson draft.");
    }

    const mergedSources = [
        ...draft.researchSources,
        ...webSources,
    ].filter((source, index, all) => {
        if (!source.url || !source.url.startsWith("http")) {
            return false;
        }
        return all.findIndex((candidate) => candidate.url === source.url) === index;
    }).slice(0, 6)
        .map((source) => ({
            title: readString(source.title, source.url).trim().slice(0, 180),
            url: readString(source.url).trim(),
            publishedDate: readString(source.publishedDate).trim().slice(0, 40),
            sourceType: readString(source.sourceType, "research").trim().slice(0, 60),
            summary: readString(source.summary).trim().slice(0, 260),
        }));

    const imagePayload = await generateOpenAIImage(
        draft.imagePrompt,
        access === DAILY_LESSON_PREMIUM_ACCESS ?
            "premium luminous editorial dream-science artwork, elegant symbolic composition" :
            "warm luminous educational dream journal artwork",
        access === DAILY_LESSON_PREMIUM_ACCESS ? 2 : 0
    );
    const imageStoragePath = isDebug && options.ownerUid ?
        `${DAILY_LESSON_STORAGE_PREFIX}/debug/${options.ownerUid}/${documentId}-${sanitizeLessonSlug(draft.title)}.png` :
        `${DAILY_LESSON_STORAGE_PREFIX}/${day}-${sanitizeLessonSlug(draft.title)}.png`;
    const imageUrl = await uploadGeneratedImage(
        options.ownerUid || "daily-lessons",
        imageStoragePath,
        imagePayload
    );
    const weekStartsOn = isoWeekStart(day);
    const document: DailyLessonDocument = {
        ...draft,
        id: documentId,
        access,
        isPremium: access === DAILY_LESSON_PREMIUM_ACCESS,
        isDebug,
        ...(options.ownerUid ? { ownerUid: options.ownerUid } : {}),
        dreamTokenAward: access === DAILY_LESSON_PREMIUM_ACCESS ? 1 : 0,
        createdDateIso: day,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        weekKey: weekStartsOn,
        weekStartsOn,
        dayIndexInWeek: dayIndexInIsoWeek(day),
        imageUrl,
        imageStoragePath,
        generationModel: model,
        imageModelHint: "gpt-image-2",
        researchSources: mergedSources,
        completed: false,
        started: false,
        bookmarked: false,
        status: "published",
    };

    await targetRef.set(document);
    return document;
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
        emotionalRadar: readEmotionRadar(payload),
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

function readEmotionRadar(payload: Record<string, unknown>): DreamEmotionRadarPayload {
    const rawRadar = payload.emotionalRadar;
    const radar = rawRadar && typeof rawRadar === "object" && !Array.isArray(rawRadar)
        ? rawRadar as Record<string, unknown>
        : {};

    return {
        joy: readRadarRatingField(radar, "joy"),
        trust: readRadarRatingField(radar, "trust"),
        fear: readRadarRatingField(radar, "fear"),
        surprise: readRadarRatingField(radar, "surprise"),
        sadness: readRadarRatingField(radar, "sadness"),
        disgust: readRadarRatingField(radar, "disgust"),
        anger: readRadarRatingField(radar, "anger"),
        anticipation: readRadarRatingField(radar, "anticipation"),
    };
}

function readRadarRatingField(payload: Record<string, unknown>, fieldName: string): number {
    const value = payload[fieldName];
    let parsed = 0;

    if (typeof value === "number") {
        parsed = value;
    } else if (typeof value === "string" && value.trim() !== "") {
        parsed = Number(value);
    }

    if (!Number.isFinite(parsed)) {
        return 0;
    }

    return Math.max(0, Math.min(5, Math.round(parsed)));
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
    const signInProvider = readString(
        (authToken as { firebase?: { sign_in_provider?: unknown } }).firebase?.sign_in_provider
    );
    if (signInProvider === "anonymous") {
        throw new functions.https.HttpsError("unauthenticated", "Must sign in to claim daily tokens.");
    }
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

exports.spendDreamTokens = functions.https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError("unauthenticated", "Must sign in to use DreamTokens.");
    }

    const uid = context.auth.uid;
    const tokensToSpend = Math.max(0, Math.round(readNumber(data?.tokensToSpend, 0)));
    const reason = readString(data?.reason, "app_purchase").trim().slice(0, 80) || "app_purchase";
    if (tokensToSpend <= 0) {
        throw new functions.https.HttpsError("invalid-argument", "tokensToSpend must be greater than zero.");
    }

    const userRef = firestore.collection("users").doc(uid);
    return firestore.runTransaction(async (transaction) => {
        const snapshot = await transaction.get(userRef);
        const userData = snapshot.data() || {};
        const currentTokens = readNumber(userData.dreamTokens, 0);
        if (currentTokens < tokensToSpend) {
            throw new functions.https.HttpsError("failed-precondition", "Not enough dream tokens available.");
        }

        const totalTokens = currentTokens - tokensToSpend;
        transaction.set(userRef, {
            uid,
            dreamTokens: totalTokens,
            lastDreamTokenSpentAt: admin.firestore.FieldValue.serverTimestamp(),
            lastDreamTokenSpendAmount: tokensToSpend,
            lastDreamTokenSpendReason: reason,
        }, { merge: true });

        return {
            success: true,
            tokensSpent: tokensToSpend,
            totalTokens,
        };
    });
});

exports.unlockDreamSymbol = functions.https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError("unauthenticated", "Must sign in to unlock dream symbols.");
    }

    const uid = context.auth.uid;
    const word = readString(data?.word).trim();
    const tokenCost = Math.max(0, Math.round(readNumber(data?.tokenCost, 0)));
    if (word.length === 0) {
        throw new functions.https.HttpsError("invalid-argument", "word is required.");
    }

    const userRef = firestore.collection("users").doc(uid);
    return firestore.runTransaction(async (transaction) => {
        const snapshot = await transaction.get(userRef);
        const userData = snapshot.data() || {};
        const unlockedWords = Array.isArray(userData.unlockedWords) ?
            userData.unlockedWords.filter((value) => typeof value === "string") as string[] :
            [];

        if (unlockedWords.includes(word)) {
            return {
                success: true,
                word,
                alreadyUnlocked: true,
                tokensSpent: 0,
                totalTokens: readNumber(userData.dreamTokens, 0),
                unlockedWords,
            };
        }

        const currentTokens = readNumber(userData.dreamTokens, 0);
        if (tokenCost > 0 && currentTokens < tokenCost) {
            throw new functions.https.HttpsError("failed-precondition", "Not enough dream tokens available.");
        }

        const totalTokens = currentTokens - tokenCost;
        const updatedUnlockedWords = [...unlockedWords, word];
        transaction.set(userRef, {
            uid,
            dreamTokens: totalTokens,
            unlockedWords: admin.firestore.FieldValue.arrayUnion(word),
            lastDreamSymbolUnlockedAt: admin.firestore.FieldValue.serverTimestamp(),
            lastDreamSymbolUnlocked: word,
            lastDreamSymbolTokenCost: tokenCost,
        }, { merge: true });

        return {
            success: true,
            word,
            alreadyUnlocked: false,
            tokensSpent: tokenCost,
            totalTokens,
            unlockedWords: updatedUnlockedWords,
        };
    });
});

exports.verifyDreamTokenPurchase = functions.https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError("unauthenticated", "Must sign in to add DreamTokens.");
    }

    const uid = context.auth.uid;
    const productId = readString(data?.productId).trim();
    const tokensFromProduct = DREAM_TOKEN_PRODUCTS[productId] || 0;
    if (tokensFromProduct <= 0) {
        throw new functions.https.HttpsError("invalid-argument", "Unknown DreamToken product.");
    }

    const transactionId = readString(data?.transactionId).trim();
    const purchaseTime = Math.max(0, Math.round(readNumber(data?.purchaseTime, 0)));
    if (transactionId.length === 0) {
        throw new functions.https.HttpsError("invalid-argument", "Missing purchase transaction id.");
    }

    const purchaseDocId = crypto
        .createHash("sha256")
        .update(`${uid}:${productId}:${transactionId}`)
        .digest("hex");
    const userRef = firestore.collection("users").doc(uid);
    const purchaseRef = userRef.collection("dream_token_purchases").doc(purchaseDocId);

    return firestore.runTransaction(async (transaction) => {
        const userSnap = await transaction.get(userRef);
        const purchaseSnap = await transaction.get(purchaseRef);
        const currentTokens = readNumber(userSnap.data()?.dreamTokens, 0);

        if (purchaseSnap.exists) {
            return {
                success: true,
                alreadyProcessed: true,
                tokensAwarded: 0,
                totalTokens: currentTokens,
            };
        }

        const totalTokens = currentTokens + tokensFromProduct;
        transaction.set(userRef, {
            uid,
            dreamTokens: totalTokens,
            lastDreamTokenPurchaseAt: admin.firestore.FieldValue.serverTimestamp(),
            lastDreamTokenPurchaseProductId: productId,
            lastDreamTokenPurchaseAmount: tokensFromProduct,
        }, { merge: true });
        transaction.set(purchaseRef, {
            productId,
            transactionId,
            purchaseTime,
            tokensAwarded: tokensFromProduct,
            createdAt: admin.firestore.FieldValue.serverTimestamp(),
        });

        return {
            success: true,
            alreadyProcessed: false,
            tokensAwarded: tokensFromProduct,
            totalTokens,
        };
    });
});

exports.generateDailyDreamLesson = functions.runWith({
    timeoutSeconds: 540,
    memory: "1GB",
    secrets: ["OPENAI_SECRET_KEY"],
}).pubsub
    .schedule("every day 04:20")
    .timeZone("America/New_York")
    .onRun(async () => {
        const day = currentUtcDay();
        const runRef = firestore.collection(DAILY_LESSON_GENERATION_RUN_COLLECTION).doc(day);
        const lessonRef = firestore.collection(DAILY_LESSON_COLLECTION).doc(day);
        const existingLesson = await lessonRef.get();
        if (existingLesson.exists) {
            await runRef.set({
                id: day,
                day,
                status: "skipped_existing",
                errorMessage: admin.firestore.FieldValue.delete(),
                failedAt: admin.firestore.FieldValue.delete(),
                updatedAt: admin.firestore.FieldValue.serverTimestamp(),
            }, { merge: true });
            return null;
        }

        await runRef.set({
            id: day,
            day,
            status: "running",
            errorMessage: admin.firestore.FieldValue.delete(),
            failedAt: admin.firestore.FieldValue.delete(),
            completedAt: admin.firestore.FieldValue.delete(),
            startedAt: admin.firestore.FieldValue.serverTimestamp(),
            updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        }, { merge: true });

        try {
            const lesson = await generateDailyLessonDocument(day);
            await runRef.set({
                id: day,
                day,
                status: "succeeded",
                lessonId: lesson.id,
                access: lesson.access,
                category: lesson.category,
                topic: lesson.topic,
                errorMessage: admin.firestore.FieldValue.delete(),
                failedAt: admin.firestore.FieldValue.delete(),
                completedAt: admin.firestore.FieldValue.serverTimestamp(),
                updatedAt: admin.firestore.FieldValue.serverTimestamp(),
            }, { merge: true });
            functions.logger.info("Generated daily dream lesson.", {
                day,
                lessonId: lesson.id,
                access: lesson.access,
                category: lesson.category,
            });
            return null;
        } catch (error) {
            const message = error instanceof Error ? error.message : "Unknown lesson generation error.";
            await runRef.set({
                id: day,
                day,
                status: "failed",
                errorMessage: message.slice(0, 600),
                failedAt: admin.firestore.FieldValue.serverTimestamp(),
                updatedAt: admin.firestore.FieldValue.serverTimestamp(),
            }, { merge: true });
            functions.logger.error("Daily dream lesson generation failed.", { day, error });
            throw error;
        }
    });

exports.generateDebugDailyDreamLesson = functions.runWith({
    timeoutSeconds: 60,
}).https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError("unauthenticated", "The function must be called while authenticated.");
    }

    const uid = context.auth.uid;
    const requestedDay = readString(data?.createdDateIso).trim();
    const day = /^\d{4}-\d{2}-\d{2}$/.test(requestedDay) ? requestedDay : currentUtcDay();
    const lessonId = `debug-${day}-${Date.now()}-${crypto.randomUUID().slice(0, 8)}`;
    const targetRef = firestore
        .collection("users")
        .doc(uid)
        .collection(DEBUG_DAILY_LESSON_REQUEST_COLLECTION)
        .doc(lessonId);

    try {
        await targetRef.set({
            id: lessonId,
            lessonId,
            uid,
            createdDateIso: day,
            status: "queued",
            createdAt: admin.firestore.FieldValue.serverTimestamp(),
            updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        });

        functions.logger.info("Queued debug daily dream lesson.", {
            uid,
            lessonId,
            day,
        });

        return {
            lessonId,
            createdDateIso: day,
        };
    } catch (error) {
        functions.logger.error("Failed to queue debug daily dream lesson.", {
            uid,
            day,
            errorMessage: error instanceof Error ? error.message : String(error),
            error,
        });
        throw new functions.https.HttpsError("internal", "Failed to queue debug lesson.");
    }
});

exports.processDebugDailyDreamLessonRequest = functions.runWith({
    timeoutSeconds: 540,
    memory: "1GB",
    secrets: ["OPENAI_SECRET_KEY"],
}).firestore
    .document(`users/{uid}/${DEBUG_DAILY_LESSON_REQUEST_COLLECTION}/{requestId}`)
    .onCreate(async (snapshot, context) => {
        const uid = readString(context.params.uid);
        const requestId = readString(context.params.requestId);
        const request = snapshot.data() || {};
        const requestedDay = readString(request.createdDateIso).trim();
        const day = /^\d{4}-\d{2}-\d{2}$/.test(requestedDay) ? requestedDay : currentUtcDay();
        const lessonId = readString(request.lessonId, requestId).trim() || requestId;
        const targetRef = firestore
            .collection("users")
            .doc(uid)
            .collection(DEBUG_DAILY_LESSON_COLLECTION)
            .doc(lessonId);

        await snapshot.ref.set({
            status: "running",
            startedAt: admin.firestore.FieldValue.serverTimestamp(),
            updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        }, { merge: true });

        try {
            const lesson = await generateDailyLessonDocument(day, {
                documentId: lessonId,
                targetRef,
                isDebug: true,
                ownerUid: uid,
                skipExistingCheck: true,
            });

            await snapshot.ref.set({
                status: "succeeded",
                lessonId: lesson.id,
                access: lesson.access,
                category: lesson.category,
                topic: lesson.topic,
                completedAt: admin.firestore.FieldValue.serverTimestamp(),
                updatedAt: admin.firestore.FieldValue.serverTimestamp(),
            }, { merge: true });

            functions.logger.info("Generated debug daily dream lesson.", {
                uid,
                lessonId: lesson.id,
                access: lesson.access,
                category: lesson.category,
            });
        } catch (error) {
            const message = error instanceof Error ? error.message : String(error);
            await snapshot.ref.set({
                status: "failed",
                errorMessage: message.slice(0, 600),
                failedAt: admin.firestore.FieldValue.serverTimestamp(),
                updatedAt: admin.firestore.FieldValue.serverTimestamp(),
            }, { merge: true });
            functions.logger.error("Debug daily dream lesson worker failed.", {
                uid,
                lessonId,
                day,
                errorMessage: message,
                stack: error instanceof Error ? error.stack : undefined,
                error,
            });
        }
    });

exports.regenerateDailyLessonSection = functions.runWith({
    timeoutSeconds: 540,
    memory: "1GB",
    secrets: ["OPENAI_SECRET_KEY"],
}).https.onCall(async (data, context) => {
    const adminIdentity = await requireDailyLessonAdmin(context);
    const lessonId = readString(data?.lessonId).trim();
    const section = readString(data?.section).trim().toLowerCase() as DailyLessonRegenerateSection;
    const instructions = readString(data?.instructions).trim().slice(0, 1600);

    if (lessonId.length === 0) {
        throw new functions.https.HttpsError("invalid-argument", "lessonId is required.");
    }
    if (![DAILY_LESSON_REGENERATE_CONTENT, DAILY_LESSON_REGENERATE_IMAGE].includes(section)) {
        throw new functions.https.HttpsError("invalid-argument", "section must be content or image.");
    }

    const lessonRef = firestore.collection(DAILY_LESSON_COLLECTION).doc(lessonId);
    const lessonSnap = await lessonRef.get();
    if (!lessonSnap.exists) {
        throw new functions.https.HttpsError("not-found", "Lesson not found.");
    }

    const lesson = {
        id: lessonSnap.id,
        ...lessonSnap.data(),
    } as Record<string, unknown>;
    const nowMillis = Date.now();

    try {
        if (section === DAILY_LESSON_REGENERATE_CONTENT) {
            const regenerated = await regenerateDailyLessonContentMarkdown(lesson, instructions);
            const mergedSources = readDailyLessonResearchSources([
                ...readDailyLessonResearchSources(lesson.researchSources),
                ...regenerated.webSources,
            ]).filter((source, index, all) =>
                all.findIndex((candidate) => candidate.url === source.url) === index
            ).slice(0, 6);

            await lessonRef.set({
                contentMarkdown: regenerated.contentMarkdown,
                researchSources: mergedSources,
                updatedAt: admin.firestore.FieldValue.serverTimestamp(),
                adminLastRegeneratedSection: section,
                adminLastRegeneratedAt: admin.firestore.FieldValue.serverTimestamp(),
                adminLastRegeneratedBy: adminIdentity,
                adminLastRegenerationInstructions: instructions.slice(0, 500),
                adminContentRegenerationModel: regenerated.model,
                adminRegenerationCount: admin.firestore.FieldValue.increment(1),
            }, { merge: true });

            functions.logger.info("Regenerated daily lesson content.", {
                lessonId,
                adminIdentity,
            });

            return {
                lessonId,
                section,
                contentMarkdown: regenerated.contentMarkdown,
            };
        }

        const jobRef = firestore.collection(DAILY_LESSON_REGENERATION_JOB_COLLECTION).doc();
        await firestore.runTransaction(async (transaction) => {
            transaction.set(jobRef, {
                id: jobRef.id,
                type: DAILY_LESSON_REGENERATION_JOB_TYPE_IMAGE,
                status: IMAGE_JOB_STATUS_QUEUED,
                lessonId,
                instructions,
                adminIdentity,
                createdAt: admin.firestore.FieldValue.serverTimestamp(),
                updatedAt: admin.firestore.FieldValue.serverTimestamp(),
            });
            transaction.set(lessonRef, {
                updatedAt: admin.firestore.FieldValue.serverTimestamp(),
                adminLastRegeneratedSection: section,
                adminLastRegeneratedAt: admin.firestore.FieldValue.serverTimestamp(),
                adminLastRegeneratedBy: adminIdentity,
                adminLastRegenerationInstructions: instructions.slice(0, 500),
                adminImageRegenerationStatus: IMAGE_JOB_STATUS_QUEUED,
                adminImageRegenerationJobId: jobRef.id,
                adminImageRegenerationQueuedAt: admin.firestore.FieldValue.serverTimestamp(),
            }, { merge: true });
        });

        functions.logger.info("Queued daily lesson image regeneration.", {
            lessonId,
            adminIdentity,
            jobId: jobRef.id,
        });

        return {
            lessonId,
            section,
            imageUrl: readString(lesson.imageUrl),
            imagePrompt: readString(lesson.imagePrompt),
            jobId: jobRef.id,
            queued: true,
        };
    } catch (error) {
        functions.logger.error("Failed to regenerate daily lesson section.", {
            lessonId,
            section,
            adminIdentity,
            error,
        });
        if (error instanceof functions.https.HttpsError) {
            throw error;
        }
        throw new functions.https.HttpsError(
            "internal",
            error instanceof Error ? error.message : "Failed to regenerate lesson section."
        );
    }
});

exports.processDailyLessonRegenerationJob = functions.runWith({
    timeoutSeconds: 540,
    memory: "1GB",
    secrets: ["OPENAI_SECRET_KEY"],
}).firestore
    .document(`${DAILY_LESSON_REGENERATION_JOB_COLLECTION}/{jobId}`)
    .onWrite(async (change, context) => {
        if (!change.after.exists) {
            return null;
        }

        const jobId = context.params.jobId;
        const jobRef = change.after.ref;
        const initialJob = change.after.data() as DailyLessonRegenerationJob;
        if (initialJob.status !== IMAGE_JOB_STATUS_QUEUED) {
            return null;
        }

        let claimedJob: DailyLessonRegenerationJob | undefined;
        await firestore.runTransaction(async (transaction) => {
            const snap = await transaction.get(jobRef);
            const latest = snap.data() as DailyLessonRegenerationJob | undefined;
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

        const lessonId = readString(job.lessonId).trim();
        const adminIdentity = readString(job.adminIdentity, "lesson-admin");
        const instructions = readString(job.instructions).trim();
        const lessonRef = firestore.collection(DAILY_LESSON_COLLECTION).doc(lessonId);

        try {
            if (job.type !== DAILY_LESSON_REGENERATION_JOB_TYPE_IMAGE) {
                throw new Error(`unsupported-lesson-regeneration-job-type:${job.type || ""}`);
            }
            if (lessonId.length === 0) {
                throw new Error("lessonId was empty.");
            }

            const lessonSnap = await lessonRef.get();
            if (!lessonSnap.exists) {
                throw new Error("Lesson not found.");
            }

            await lessonRef.set({
                adminImageRegenerationStatus: IMAGE_JOB_STATUS_RUNNING,
                adminImageRegenerationJobId: jobId,
                adminImageRegenerationStartedAt: admin.firestore.FieldValue.serverTimestamp(),
                updatedAt: admin.firestore.FieldValue.serverTimestamp(),
            }, { merge: true });

            const lesson = {
                id: lessonSnap.id,
                ...lessonSnap.data(),
            } as Record<string, unknown>;
            const imagePrompt = await regenerateDailyLessonImagePrompt(lesson, instructions);
            if (imagePrompt.length === 0) {
                throw new Error("Image prompt was empty.");
            }

            const access = readString(lesson.access);
            const title = readString(lesson.title, lessonId);
            const day = readString(lesson.createdDateIso, lessonId);
            const nowMillis = Date.now();
            const imagePayload = await generateOpenAIImage(
                imagePrompt,
                access === DAILY_LESSON_PREMIUM_ACCESS ?
                    "premium luminous editorial dream-science artwork, elegant symbolic composition" :
                    "warm luminous educational dream journal artwork",
                access === DAILY_LESSON_PREMIUM_ACCESS ? 2 : 0
            );
            const imageStoragePath = `${DAILY_LESSON_STORAGE_PREFIX}/${day}-${sanitizeLessonSlug(title)}-admin-${nowMillis}.png`;
            const imageUrl = await uploadGeneratedImage(
                "daily-lessons",
                imageStoragePath,
                imagePayload
            );

            await lessonRef.set({
                imagePrompt,
                imageUrl,
                imageStoragePath,
                updatedAt: admin.firestore.FieldValue.serverTimestamp(),
                adminLastRegeneratedSection: DAILY_LESSON_REGENERATE_IMAGE,
                adminLastRegeneratedAt: admin.firestore.FieldValue.serverTimestamp(),
                adminLastRegeneratedBy: adminIdentity,
                adminLastRegenerationInstructions: instructions.slice(0, 500),
                adminImageRegenerationModel: process.env.LESSON_IMAGE_PROMPT_MODEL || "gpt-5.4-mini",
                adminImageRegenerationStatus: IMAGE_JOB_STATUS_SUCCEEDED,
                adminImageRegenerationJobId: jobId,
                adminImageRegenerationCompletedAt: admin.firestore.FieldValue.serverTimestamp(),
                imageModelHint: "gpt-image-2",
                adminRegenerationCount: admin.firestore.FieldValue.increment(1),
            }, { merge: true });

            await jobRef.update({
                status: IMAGE_JOB_STATUS_SUCCEEDED,
                imagePrompt,
                imageUrl,
                completedAt: admin.firestore.FieldValue.serverTimestamp(),
                updatedAt: admin.firestore.FieldValue.serverTimestamp(),
            });

            functions.logger.info("Regenerated daily lesson image from queued job.", {
                lessonId,
                jobId,
                adminIdentity,
                imageStoragePath,
            });
            return null;
        } catch (error) {
            const message = error instanceof Error ? error.message : "Unknown lesson image regeneration error.";
            functions.logger.error("Daily lesson image regeneration job failed.", {
                lessonId,
                jobId,
                adminIdentity,
                message,
                error,
            });
            await jobRef.update({
                status: IMAGE_JOB_STATUS_FAILED,
                errorMessage: message.slice(0, 600),
                failedAt: admin.firestore.FieldValue.serverTimestamp(),
                updatedAt: admin.firestore.FieldValue.serverTimestamp(),
            }).catch((updateError) =>
                functions.logger.warn("Failed to mark lesson image regeneration job failed.", updateError)
            );
            if (lessonId.length > 0) {
                await lessonRef.set({
                    adminImageRegenerationStatus: IMAGE_JOB_STATUS_FAILED,
                    adminImageRegenerationJobId: jobId,
                    adminImageRegenerationErrorMessage: message.slice(0, 600),
                    adminImageRegenerationFailedAt: admin.firestore.FieldValue.serverTimestamp(),
                    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
                }, { merge: true }).catch((updateError) =>
                    functions.logger.warn("Failed to mark lesson image regeneration failed.", updateError)
                );
            }
            return null;
        }
    });

exports.completeDailyLesson = functions.runWith({
    secrets: ["REVENUECAT_SECRET_KEY"],
}).https.onCall(async (data, context): Promise<DailyLessonCompletionResponse> => {
    if (!context.auth) {
        throw new functions.https.HttpsError("unauthenticated", "The function must be called while authenticated.");
    }

    const uid = context.auth.uid;
    const lessonId = readString(data?.lessonId).trim();
    const selectedAnswers = data?.selectedAnswers && typeof data.selectedAnswers === "object" && !Array.isArray(data.selectedAnswers) ?
        data.selectedAnswers as Record<string, unknown> :
        {};
    if (lessonId.length === 0) {
        throw new functions.https.HttpsError("invalid-argument", "lessonId is required.");
    }

    const userRef = firestore.collection("users").doc(uid);
    const progressRef = userRef.collection("lesson_progress").doc(lessonId);

    try {
        const lessonRef = firestore.collection(DAILY_LESSON_COLLECTION).doc(lessonId);
        let lessonSnap = await lessonRef.get();
        if (!lessonSnap.exists) {
            lessonSnap = await userRef
                .collection(DEBUG_DAILY_LESSON_COLLECTION)
                .doc(lessonId)
                .get();
        }
        if (!lessonSnap.exists) {
            throw new functions.https.HttpsError("not-found", "Lesson not found.");
        }

        const lesson = lessonSnap.data() || {};
        const questions = Array.isArray(lesson.questions) ? lesson.questions as DailyLessonQuizQuestion[] : [];
        const isDebugLesson = lesson.isDebug === true;
        const isPremiumLesson = readString(lesson.access) === DAILY_LESSON_PREMIUM_ACCESS ||
            lesson.isPremium === true;
        if (isPremiumLesson && !isDebugLesson) {
            const hasPremiumEntitlement = await hasRevenueCatPremiumEntitlement(uid);
            if (!hasPremiumEntitlement) {
                functions.logger.warn("Completing premium lesson without backend entitlement match.", {
                    uid,
                    lessonId,
                });
            }
        }

        const normalizeAnswerId = (value: unknown): string => readString(value).trim().toLowerCase();
        const legacyAnswerId = (value: unknown): string => {
            const normalized = normalizeAnswerId(value);
            if (/^[a-d]$/.test(normalized)) {
                return normalized;
            }
            return normalized.match(/(?:^|[^a-z])([a-d])(?:$|[^a-z])/)?.[1] ||
                normalized.match(/[a-d]$/)?.[0] ||
                normalized.slice(0, 1);
        };
        const normalizedAnswers: Record<string, string> = {};
        questions.forEach((question) => {
            const questionId = readString(question.id).trim();
            const selected = normalizeAnswerId(selectedAnswers[questionId]);
            const validOptionIds = Array.isArray(question.options) ?
                question.options.map((option) => normalizeAnswerId(option.id)).filter((id) => id !== "") :
                [];
            const selectedLegacy = legacyAnswerId(selected);
            const matchedOptionId = validOptionIds.includes(selected) ?
                selected :
                validOptionIds.find((optionId) => legacyAnswerId(optionId) === selectedLegacy);

            if (questionId !== "" && matchedOptionId) {
                normalizedAnswers[questionId] = matchedOptionId;
            }
        });

        if (questions.some((question) => !normalizedAnswers[readString(question.id).trim()])) {
            throw new functions.https.HttpsError("failed-precondition", "Answer all lesson questions before completing.");
        }

        const quizScore = questions.reduce((score, question) => {
            const questionId = readString(question.id).trim();
            const selected = normalizedAnswers[questionId];
            const correctOptionId = normalizeAnswerId(question.correctOptionId);
            const validOptionIds = Array.isArray(question.options) ?
                question.options.map((option) => normalizeAnswerId(option.id)).filter((id) => id !== "") :
                [];
            const matchedCorrectOptionId = validOptionIds.includes(correctOptionId) ?
                correctOptionId :
                validOptionIds.find((optionId) => legacyAnswerId(optionId) === legacyAnswerId(correctOptionId));
            const isCorrect = selected === (matchedCorrectOptionId || correctOptionId);
            return isCorrect ?
                score + 1 :
                score;
        }, 0);
        if (questions.length === 0 || quizScore < questions.length) {
            throw new functions.https.HttpsError(
                "failed-precondition",
                "Answer every lesson question correctly before completing."
            );
        }
        const award = Math.max(0, Math.round(readNumber(lesson.dreamTokenAward, isPremiumLesson ? 1 : 0)));

        return await firestore.runTransaction(async (transaction) => {
            const progressSnap = await transaction.get(progressRef);
            const progress = progressSnap.data() || {};
            const alreadyCompleted = progress.completed === true;
            const tokensAwarded = alreadyCompleted ? 0 : award;
            const userSnap = await transaction.get(userRef);
            const totalTokens = readNumber(userSnap.data()?.dreamTokens, 0) + tokensAwarded;
            const nowMillis = Date.now();

            transaction.set(progressRef, {
                lessonId,
                started: true,
                completed: true,
                selectedAnswers: normalizedAnswers,
                quizScore,
                questionCount: questions.length,
                tokensAwarded: readNumber(progress.tokensAwarded, 0) + tokensAwarded,
                completedAtMillis: readNumber(progress.completedAtMillis, nowMillis),
                updatedAtMillis: nowMillis,
                completedAt: progress.completedAt || admin.firestore.FieldValue.serverTimestamp(),
                updatedAt: admin.firestore.FieldValue.serverTimestamp(),
            }, { merge: true });

            if (tokensAwarded > 0) {
                transaction.set(userRef, {
                    uid,
                    dreamTokens: admin.firestore.FieldValue.increment(tokensAwarded),
                    lastLessonTokenAwardedAt: admin.firestore.FieldValue.serverTimestamp(),
                    lastLessonTokenAwardedLessonId: lessonId,
                }, { merge: true });
            }

            return {
                lessonId,
                completed: true,
                tokensAwarded,
                totalTokens,
                quizScore,
            };
        });
    } catch (error) {
        if (error instanceof functions.https.HttpsError) {
            throw error;
        }
        functions.logger.error("Failed to complete daily lesson.", { uid, lessonId, error });
        throw new functions.https.HttpsError("internal", "Failed to complete lesson.");
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
        const interaction: any = await client.interactions.create({
            model: GEMINI_FAST_MODEL,
            input: [
                { type: "text", text: "Transcribe the speech in this audio. Return only the transcript." },
                {
                    type: "audio",
                    data: base64Audio,
                    mime_type: "audio/m4a",
                },
            ],
            store: false,
        } as any);
        const text = getInteractionText(interaction);

        if (!text) {
            throw new Error("Empty response from Gemini");
        }

        return { text: text };

    } catch (error) {
        if (error instanceof functions.https.HttpsError) {
            throw error;
        }
        functions.logger.error("Transcription error:", error);
        throw new functions.https.HttpsError('unavailable', "Couldn't transcribe. Try again.", error);
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
        const prompt = `
            Analyze the following dream content and return a JSON object with the specified structure.
            - isLucid: boolean category flag. true only if the dreamer knew they were dreaming or controlled the dream.
            - isNightmare: boolean category flag. true only if the dream is clearly scary, threatening, or distressing.
            - isRecurring: boolean category flag. true only if the text says this dream, setting, theme, or event repeats.
            - isFalseAwakening: boolean category flag. true only if the dream involves waking up inside the dream or believing they woke up.
            - lucidity: integer score from 1-5. 1 means no awareness; 5 means strong awareness/control.
            - vividness: integer score from 1-5. 1 means vague; 5 means richly detailed and sensory.
            - mood: integer (a score from 1-5 for mood, 1 being very negative, 5 being very positive)
            - emotionalRadar: object with integer scores from 0-5 for these exact keys:
              joy, trust, fear, surprise, sadness, disgust, anger, anticipation.
              Interpret trust as warmth, safety, affection, and love in the dream.
              Use 0 when the emotion is absent, 1-2 when subtle, 3-4 when clear, and 5 when dominant.

            Only return the JSON object, nothing else.

            Dream Content: "${dreamContent}"
        `;

        functions.logger.info("Sending prompt to Gemini API");

        const client = await getGenAIClient();
        const interaction: any = await client.interactions.create({
            model: GEMINI_FAST_MODEL,
            input: prompt,
            store: false,
        } as any);
        const rawResponse = getInteractionText(interaction);

        if (!rawResponse) {
            functions.logger.error("Empty response from Gemini");
            throw new Error("Empty response from Gemini");
        }

        const finalResult = parseCategorizationResponse(rawResponse);

        functions.logger.info("Returning result:", { result: finalResult });
        return finalResult;

    } catch (error) {
        functions.logger.error("Error in categorizeDream:", error);
        throw new functions.https.HttpsError('unavailable', 'Dream categorization failed. Try again.', error);
    }
});
exports.generateDreamTitle = functions.runWith({
    timeoutSeconds: 120,
    memory: "512MB",
    secrets: ["OPENAI_SECRET_KEY"],
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
        const message = providerErrorMessage(error, "Dream title generation failed.");
        functions.logger.error("Dream title generation failed.", {
            uid: context.auth.uid,
            message,
            error: safeErrorLog(error),
        });
        throw new functions.https.HttpsError("internal", message);
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
                    imageGenerationErrorCode: "",
                    imageGenerationErrorMessage: "",
                }) as FirebaseFirestore.UpdateData<FirebaseFirestore.DocumentData>
            );
        });

        return { jobId: jobRef.id };
    } catch (error) {
        if (error instanceof functions.https.HttpsError) {
            throw error;
        }
        functions.logger.error("Failed to enqueue dream image generation.", {
            uid,
            dreamId,
            error: safeErrorLog(error),
        });
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
        functions.logger.error("Failed to enqueue dream world generation.", {
            uid,
            error: safeErrorLog(error),
        });
        throw new functions.https.HttpsError("internal", "Failed to enqueue dream world generation.");
    }
});

exports.processImageGenerationJob = functions.runWith({
    timeoutSeconds: 540,
    memory: "1GB",
    secrets: ["OPENAI_SECRET_KEY"],
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
                        imageGenerationErrorCode: "",
                        imageGenerationErrorMessage: "",
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
            const message = providerErrorMessage(error, "Image generation failed.");
            functions.logger.error("Image generation job failed.", {
                uid,
                jobId,
                message,
                error: safeErrorLog(error),
            });
            await refundReservedTokensIfNeeded(uid, jobRef, job, "generation_failed", message);

            if (job.type === IMAGE_JOB_TYPE_DREAM && job.targetDreamId) {
                await firestore.collection("users").doc(uid).collection("my_dreams").doc(job.targetDreamId)
                    .update(imageGenerationStatusFields(IMAGE_JOB_STATUS_FAILED, jobId, {
                        imageGenerationErrorCode: "generation_failed",
                        imageGenerationErrorMessage: message,
                    }))
                    .catch((updateError) => functions.logger.warn("Failed to mark dream image job failed.", updateError));
            } else if (job.type === IMAGE_JOB_TYPE_WORLD && job.targetPaintingId) {
                await firestore.collection("users").doc(uid).collection("dream_world_paintings").doc(job.targetPaintingId)
                    .update({
                        status: IMAGE_JOB_STATUS_FAILED,
                        errorCode: "generation_failed",
                        errorMessage: message,
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

        const anonymousDreamTokens = Math.max(
            0,
            Math.round(readNumber(anonymousUserData.dreamTokens, 0))
        );
        const anonymousUnlockedWords = readStringArray(anonymousUserData.unlockedWords);
        const tokenTransferRef = permanentUserRef
            .collection("account_link_transfers")
            .doc(anonymousUid);
        let tokensTransferred = 0;
        let symbolsTransferred = 0;

        await firestore.runTransaction(async (transaction) => {
            const permanentUserSnapshot = await transaction.get(permanentUserRef);
            const tokenTransferSnapshot = await transaction.get(tokenTransferRef);
            const permanentUserData = permanentUserSnapshot.data() || {};
            const permanentDreamTokens = readNumber(permanentUserData.dreamTokens, 0);
            const permanentUnlockedWords = readStringArray(permanentUserData.unlockedWords);
            const permanentUnlockedWordSet = new Set(permanentUnlockedWords);
            const wordsToTransfer = anonymousUnlockedWords
                .filter((word) => !permanentUnlockedWordSet.has(word));
            tokensTransferred = tokenTransferSnapshot.exists ? 0 : anonymousDreamTokens;
            symbolsTransferred = wordsToTransfer.length;
            const permanentUserTransferFields: Record<string, unknown> = {
                dreamTokens: permanentDreamTokens + tokensTransferred,
                accountLinkedFromAnonymousUid: anonymousUid,
                accountLinkedAt: admin.firestore.FieldValue.serverTimestamp(),
                ...(tokensTransferred > 0 ? {
                    lastAnonymousDreamTokensTransferred: tokensTransferred,
                    lastAnonymousDreamTokensTransferredAt:
                        admin.firestore.FieldValue.serverTimestamp(),
                } : {}),
                ...(wordsToTransfer.length > 0 ? {
                    unlockedWords: admin.firestore.FieldValue.arrayUnion(...wordsToTransfer),
                    lastAnonymousSymbolsTransferred: wordsToTransfer.length,
                    lastAnonymousSymbolsTransferredAt:
                        admin.firestore.FieldValue.serverTimestamp(),
                } : {}),
            };

            transaction.set(
                permanentUserRef,
                permanentUserTransferFields,
                { merge: true }
            );

            if (!tokenTransferSnapshot.exists) {
                transaction.set(tokenTransferRef, {
                    anonymousUid,
                    permanentUid,
                    dreamTokensTransferred: tokensTransferred,
                    symbolsTransferred,
                    createdAt: admin.firestore.FieldValue.serverTimestamp(),
                });
            } else if (symbolsTransferred > 0) {
                transaction.set(tokenTransferRef, {
                    anonymousUid,
                    permanentUid,
                    symbolsTransferred: admin.firestore.FieldValue.increment(symbolsTransferred),
                    updatedAt: admin.firestore.FieldValue.serverTimestamp(),
                }, { merge: true });
            }
        });

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

        const lessonTransfer = await transferLessonProgressForAccountLinking(
            anonymousUid,
            permanentUid
        );

        return {
            success: true,
            message: "Account data transferred successfully.",
            tokensTransferred,
            symbolsTransferred,
            lessonProgressTransferred: lessonTransfer.lessonProgressTransferred,
            lessonCompletionsTransferred: lessonTransfer.lessonCompletionsTransferred,
        };
    } catch (error) {
        throw new functions.https.HttpsError("internal", "Error during account linking process.");
    }
});


export const handleUserCreate = functions.auth.user().onCreate(async (user) => {
    const isAnonymous = user.providerData.length === 0;
    const isGoogleSignIn = user.providerData
        .some((provider) => provider.providerId === "google.com");

    const userRef = firestore.collection("users").doc(user.uid);
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

    await firestore.runTransaction(async (transaction) => {
        const snapshot = await transaction.get(userRef);
        if (!snapshot.exists) {
            transaction.set(userRef, newUser);
            return;
        }

        const existing = snapshot.data() || {};
        const preservedUser: Record<string, unknown> = {
            uid: user.uid,
            displayName: user.displayName || existing.displayName || "Anonymous",
            email: user.email || existing.email || "",
            emailVerified: isGoogleSignIn || user.emailVerified || existing.emailVerified || false,
            lastActiveTimestamp: admin.firestore.FieldValue.serverTimestamp(),
        };

        if (existing.dreamTokens === undefined || existing.dreamTokens === null) {
            preservedUser.dreamTokens = isAnonymous ? 0 : 25;
        }
        if (existing.unlockedWords === undefined) {
            preservedUser.unlockedWords = [];
        }
        if (existing.hasCompletedOnboarding === undefined) {
            preservedUser.hasCompletedOnboarding = false;
        }
        if (existing.registrationTimestamp === undefined) {
            preservedUser.registrationTimestamp = admin.firestore.FieldValue.serverTimestamp();
        }

        transaction.set(userRef, preservedUser, { merge: true });
    });
});

const purchaseVerificationApp = express();
purchaseVerificationApp.use(cors({ origin: true }));
purchaseVerificationApp.use(express.json());

purchaseVerificationApp.post("/", async (req, res) => {
    const data = req.body?.data || req.body || {};
    const userId = readString(data.userId).trim();
    const dreamTokens = Math.max(0, Math.round(readNumber(data.dreamTokens, 0)));
    const transactionId = readString(
        data.transactionId ||
        data.purchaseToken ||
        data.orderId ||
        `${readString(data.productId, "legacy")}-${readNumber(data.purchaseTime, Date.now())}`
    ).trim();

    if (userId.length === 0 || dreamTokens <= 0) {
        res.status(200).json({
            result: {
                success: false,
                message: "Missing user or token amount.",
            },
        });
        return;
    }

    try {
        const userRef = firestore.collection("users").doc(userId);
        const purchaseDocId = crypto
            .createHash("sha256")
            .update(`${userId}:legacy:${transactionId}`)
            .digest("hex");
        const purchaseRef = userRef.collection("dream_token_purchases").doc(purchaseDocId);

        const result = await firestore.runTransaction(async (transaction) => {
            const userSnap = await transaction.get(userRef);
            const purchaseSnap = await transaction.get(purchaseRef);
            const currentTokens = readNumber(userSnap.data()?.dreamTokens, 0);

            if (purchaseSnap.exists) {
                return {
                    success: true,
                    alreadyProcessed: true,
                    tokensAwarded: 0,
                    totalTokens: currentTokens,
                };
            }

            const totalTokens = currentTokens + dreamTokens;
            transaction.set(userRef, {
                uid: userId,
                dreamTokens: totalTokens,
                lastDreamTokenPurchaseAt: admin.firestore.FieldValue.serverTimestamp(),
                lastDreamTokenPurchaseAmount: dreamTokens,
            }, { merge: true });
            transaction.set(purchaseRef, {
                transactionId,
                tokensAwarded: dreamTokens,
                source: "legacy_handlePurchaseVerification",
                createdAt: admin.firestore.FieldValue.serverTimestamp(),
            });

            return {
                success: true,
                alreadyProcessed: false,
                tokensAwarded: dreamTokens,
                totalTokens,
            };
        });

        res.status(200).json({ result });
    } catch (error) {
        functions.logger.error("Legacy purchase verification failed.", {
            userId,
            dreamTokens,
            error: safeErrorLog(error),
        });
        res.status(200).json({
            result: {
                success: false,
                message: "Purchase verification failed.",
            },
        });
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
    const resendConfig = functions.config().resend || {};
    const resendApiKey = resendConfig.api_key;
    const sender = resendConfig.sender || "DreamNorth by SortoApps <dreamnorth-noreply@sorto-apps.com>";
    const heroImageUrl = resendConfig.hero_url;
    const subject = "Complete Your Registration with DreamNorth";
    const preheader = "Verify your email to finish setting up your DreamNorth account.";
    const text = `Welcome to DreamNorth!

Thanks for signing up. Verify your email to finish setting up your account:
${verificationLink}

If you did not sign up for a DreamNorth account, you can safely ignore this email.

DreamNorth by SortoApps`;
    const heroImageMarkup = heroImageUrl ? `
                                <img src="${heroImageUrl}" width="600" alt="DreamNorth lighthouse under a starlit sky" style="display:block; width:100%; max-width:600px; height:auto; border:0; line-height:100%; outline:none; text-decoration:none;">
                            ` : "";
    const html = `
        <!doctype html>
        <html lang="en">
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <meta name="color-scheme" content="light dark">
            <meta name="supported-color-schemes" content="light dark">
            <title>Verify your DreamNorth email</title>
        </head>
        <body style="margin:0; padding:0; background-color:#07031A;">
            <div style="display:none; max-height:0; overflow:hidden; opacity:0; color:transparent;">
                ${preheader}
            </div>
            <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="background-color:#07031A; margin:0; padding:0;">
                <tr>
                    <td align="center" style="padding:28px 14px;">
                        <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="width:100%; max-width:600px; background-color:#120A2E; border-radius:22px; overflow:hidden; border:1px solid #2E2364;">
                            <tr>
                                <td style="background-color:#120A2E;">
                                    ${heroImageMarkup}
                                </td>
                            </tr>
                            <tr>
                                <td style="padding:30px 28px 10px 28px; font-family:Arial, Helvetica, sans-serif;">
                                    <div style="font-size:13px; line-height:18px; letter-spacing:0.08em; text-transform:uppercase; color:#FFD59D; font-weight:700;">
                                        DreamNorth
                                    </div>
                                    <h1 style="margin:10px 0 12px 0; color:#FFFFFF; font-size:30px; line-height:36px; font-weight:800;">
                                        Verify your email
                                    </h1>
                                    <p style="margin:0; color:#E9E3FF; font-size:16px; line-height:25px;">
                                        Thanks for signing up. Confirm this email address to finish setting up your DreamNorth account and keep your dreams safely connected to you.
                                    </p>
                                </td>
                            </tr>
                            <tr>
                                <td align="center" style="padding:22px 28px 24px 28px; font-family:Arial, Helvetica, sans-serif;">
                                    <a href="${verificationLink}" style="display:inline-block; background-color:#FFB45F; color:#1A1034; text-decoration:none; font-size:16px; line-height:20px; font-weight:800; padding:15px 26px; border-radius:14px;">
                                        Verify email
                                    </a>
                                </td>
                            </tr>
                            <tr>
                                <td style="padding:0 28px 28px 28px; font-family:Arial, Helvetica, sans-serif;">
                                    <p style="margin:0 0 14px 0; color:#BEB6DE; font-size:13px; line-height:20px;">
                                        If the button does not work, copy and paste this link into your browser:
                                    </p>
                                    <p style="margin:0; word-break:break-all; color:#9DD7FF; font-size:12px; line-height:18px;">
                                        <a href="${verificationLink}" style="color:#9DD7FF; text-decoration:underline;">${verificationLink}</a>
                                    </p>
                                </td>
                            </tr>
                            <tr>
                                <td style="padding:20px 28px 28px 28px; font-family:Arial, Helvetica, sans-serif; background-color:#0B0620; border-top:1px solid #2A2154;">
                                    <p style="margin:0 0 8px 0; color:#D9D1F3; font-size:13px; line-height:20px;">
                                        Did not create a DreamNorth account? You can safely ignore this email.
                                    </p>
                                    <p style="margin:0; color:#8F87AF; font-size:12px; line-height:18px;">
                                        DreamNorth by SortoApps<br>
                                        This is a transactional account verification email.
                                    </p>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </body>
        </html>
        `;

    if (!resendApiKey) {
        functions.logger.error("Resend API key is not configured.");
        throw new functions.https.HttpsError("failed-precondition", "Verification email sender is not configured");
    }

    try {
        await axios.post(
            "https://api.resend.com/emails",
            {
                from: sender,
                to: [userEmail],
                subject,
                text,
                html
            },
            {
                headers: {
                    Authorization: `Bearer ${resendApiKey}`,
                    "Content-Type": "application/json"
                }
            }
        );
    } catch (error) {
        functions.logger.error("Failed to send verification email through Resend", {
            userEmail,
            error
        });
        throw new functions.https.HttpsError("internal", "Unable to send verification email");
    }
}
