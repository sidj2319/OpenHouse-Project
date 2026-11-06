import nodemailer, { type Transporter } from "nodemailer";
import { EmailDeliveryError, type EmailMessage, type EmailSender } from "./emailSender";

export interface SmtpSettings {
  host: string;
  port: number;
  user?: string;
  pass?: string;
}

export class SmtpEmailSender implements EmailSender {
  private readonly transporter: Transporter;

  constructor(settings: SmtpSettings) {
    this.transporter = nodemailer.createTransport({
      host: settings.host,
      port: settings.port,
      secure: settings.port === 465,
      auth: settings.user ? { user: settings.user, pass: settings.pass ?? "" } : undefined,
      connectionTimeout: 5000,
      socketTimeout: 10000,
    });
  }

  async send(message: EmailMessage): Promise<void> {
    try {
      await this.transporter.sendMail(message);
    } catch (err) {
      throw new EmailDeliveryError("Failed to send email", { cause: err });
    }
  }
}
