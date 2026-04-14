import { makeHandler } from '@keystatic/astro/api';
import keystatic from '../../../../keystatic.config';
import { env } from 'cloudflare:workers';

export const prerender = false;

/**
 * Pass env vars directly to makeHandler config to bypass the removed
 * context.locals.runtime.env accessor in @astrojs/cloudflare v13.
 */
export const ALL = makeHandler({
	config: keystatic,
	clientId: env.KEYSTATIC_GITHUB_CLIENT_ID,
	clientSecret: env.KEYSTATIC_GITHUB_CLIENT_SECRET,
	secret: env.KEYSTATIC_SECRET,
});
