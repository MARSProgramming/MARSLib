import { makeHandler } from '@keystatic/astro/api';
import keystatic from '../../../../keystatic.config';
import type { APIContext } from 'astro';

export const prerender = false;

/**
 * Custom handler that lazily resolves Cloudflare env vars at request time.
 *
 * @astrojs/cloudflare v13 removed context.locals.runtime.env and Keystatic's
 * makeHandler still tries to read from it, causing a runtime crash.
 * We import cloudflare:workers dynamically at request time and pass the
 * resolved env vars directly into the config.
 */
export const ALL = async (context: APIContext) => {
	const { env } = await import('cloudflare:workers');

	const handler = makeHandler({
		config: keystatic,
		clientId: env.KEYSTATIC_GITHUB_CLIENT_ID,
		clientSecret: env.KEYSTATIC_GITHUB_CLIENT_SECRET,
		secret: env.KEYSTATIC_SECRET,
	});

	return handler(context);
};
