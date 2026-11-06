import express, { type NextFunction, type Request, type RequestHandler, type Response } from "express";
import helmet from "helmet";
import rateLimit from "express-rate-limit";
import { z, ZodError } from "zod";
import { EmailDeliveryError } from "./email/emailSender";
import { ProductSourceError } from "./providers/productSource";
import type { CatalogueService } from "./services/catalogueService";
import { ProductNotFoundError, type EnquiryService } from "./services/enquiryService";

export interface AppDeps {
  catalogue: CatalogueService;
  enquiries: EnquiryService;
}

const asyncHandler =
  (fn: (req: Request, res: Response) => Promise<void>): RequestHandler =>
  (req, res, next) => {
    fn(req, res).catch(next);
  };

const idSchema = z.coerce.number().int().positive();
const searchSchema = z.object({ q: z.string().trim().min(1).max(100) });
const categoryNameSchema = z.string().trim().min(1).max(100);

const enquirySchema = z.object({
  productId: z.number().int().positive(),
  name: z.string().trim().min(1).max(100),
  email: z.string().trim().email().max(254),
  phone: z
    .string()
    .trim()
    .regex(/^[+0-9()\-\s]{5,30}$/, "Invalid phone number")
    .optional()
    .or(z.literal("").transform(() => undefined)),
  message: z.string().trim().max(1000).optional(),
});

export function createApp({ catalogue, enquiries }: AppDeps) {
  const app = express();
  app.disable("x-powered-by");
  app.use(helmet());
  app.use(express.json({ limit: "10kb" }));

  app.get("/health", (_req, res) => {
    res.json({ status: "ok" });
  });

  const api = express.Router();

  api.get(
    "/home",
    asyncHandler(async (_req, res) => {
      res.json({ featured: await catalogue.featured() });
    }),
  );

  api.get(
    "/categories",
    asyncHandler(async (_req, res) => {
      res.json({ categories: await catalogue.categories() });
    }),
  );

  api.get(
    "/categories/:name",
    asyncHandler(async (req, res) => {
      const name = categoryNameSchema.parse(req.params.name);
      const listing = await catalogue.category(name);
      if (!listing) {
        res.status(404).json({ error: "Category not found" });
        return;
      }
      res.json(listing);
    }),
  );

  api.get(
    "/products",
    asyncHandler(async (req, res) => {
      const { q } = searchSchema.parse(req.query);
      res.json({ query: q, products: await catalogue.search(q) });
    }),
  );

  api.get(
    "/products/:id",
    asyncHandler(async (req, res) => {
      const id = idSchema.parse(req.params.id);
      const product = await catalogue.get(id);
      if (!product) {
        res.status(404).json({ error: "Product not found" });
        return;
      }
      res.json({ product });
    }),
  );

  api.post(
    "/enquiries",
    rateLimit({ windowMs: 60_000, limit: 10, standardHeaders: true, legacyHeaders: false }),
    asyncHandler(async (req, res) => {
      const input = enquirySchema.parse(req.body);
      await enquiries.submit(input);
      res.status(201).json({ ok: true });
    }),
  );

  app.use("/api", api);

  app.use((_req, res) => {
    res.status(404).json({ error: "Not found" });
  });

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  app.use((err: unknown, _req: Request, res: Response, _next: NextFunction) => {
    if (err instanceof ZodError) {
      res.status(400).json({
        error: "Invalid request",
        details: err.issues.map((i) => ({ field: i.path.join("."), message: i.message })),
      });
      return;
    }
    if (err instanceof ProductNotFoundError) {
      res.status(404).json({ error: "Product not found" });
      return;
    }
    if (err instanceof ProductSourceError) {
      console.error(err);
      res.status(502).json({ error: "Product data is temporarily unavailable" });
      return;
    }
    if (err instanceof EmailDeliveryError) {
      console.error(err);
      res.status(503).json({ error: "We couldn't send your enquiry. Please try again shortly." });
      return;
    }
    const status = (err as { status?: number } | null)?.status;
    if (typeof status === "number" && status >= 400 && status < 500) {
      // e.g. malformed JSON or oversized body from express.json()
      res.status(status).json({ error: "Invalid request" });
      return;
    }
    console.error(err);
    res.status(500).json({ error: "Internal server error" });
  });

  return app;
}
