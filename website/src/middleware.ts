import { defineMiddleware } from 'astro:middleware';

/**
 * Polyfill for @astrojs/cloudflare v13 breaking change.
 *
 * v13 removed `context.locals.runtime.env` in favor of `import { env } from 'cloudflare:workers'`.
 * However, @keystatic/astro/api still reads env vars from `context.locals.runtime.env`.
 * This middleware re-injects the Cloudflare env bindings into the old location
 * to maintain backward compatibility with Keystatic's handler.
 */
export const onRequest = defineMiddleware(async (context, next) => {
	try {
		// Dynamic import to avoid build-time errors in non-CF environments
		const { env } = await import('cloudflare:workers');
		// @ts-ignore - polyfill for legacy Keystatic compatibility
		context.locals.runtime = { env };
	} catch {
		// Not running on Cloudflare (e.g., local dev without wrangler)
	}
	return next();
});
