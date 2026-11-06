import { z } from "zod";

const envSchema = z.object({
  PORT: z.coerce.number().int().positive().default(3000),
  FAKE_STORE_BASE_URL: z.string().url().default("https://fakestoreapi.com"),
  PRODUCT_CACHE_TTL_SECONDS: z.coerce.number().int().min(0).default(60),
  FEATURED_PRODUCT_IDS: z.string().default(""),
  SALES_INBOX_EMAIL: z.string().email().default("sales@example.com"),
  EMAIL_FROM: z.string().min(3).default("Catalogue <no-reply@example.com>"),
  SMTP_HOST: z.string().default(""),
  SMTP_PORT: z.coerce.number().int().positive().default(1025),
  SMTP_USER: z.string().default(""),
  SMTP_PASS: z.string().default(""),
});

export interface AppConfig {
  port: number;
  fakeStoreBaseUrl: string;
  productCacheTtlMs: number;
  featuredProductIds: number[];
  salesInboxEmail: string;
  emailFrom: string;
  smtp: { host: string; port: number; user?: string; pass?: string } | null;
}

function parseIds(raw: string): number[] {
  return raw
    .split(",")
    .map((s) => s.trim())
    .filter(Boolean)
    .map(Number)
    .filter((n) => Number.isInteger(n) && n > 0);
}

/** Fails fast with a readable error if the environment is misconfigured. */
export function loadConfig(env: NodeJS.ProcessEnv = process.env): AppConfig {
  const e = envSchema.parse(env);
  return {
    port: e.PORT,
    fakeStoreBaseUrl: e.FAKE_STORE_BASE_URL,
    productCacheTtlMs: e.PRODUCT_CACHE_TTL_SECONDS * 1000,
    featuredProductIds: parseIds(e.FEATURED_PRODUCT_IDS),
    salesInboxEmail: e.SALES_INBOX_EMAIL,
    emailFrom: e.EMAIL_FROM,
    smtp: e.SMTP_HOST
      ? {
          host: e.SMTP_HOST,
          port: e.SMTP_PORT,
          ...(e.SMTP_USER ? { user: e.SMTP_USER, pass: e.SMTP_PASS } : {}),
        }
      : null,
  };
}
