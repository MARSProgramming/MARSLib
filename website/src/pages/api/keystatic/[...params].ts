import { makeHandler } from '@keystatic/astro/api';
import keystatic from '../../../../keystatic.config';
import type { APIContext } from 'astro';

export const prerender = false;

/**
 * Custom wrapper for Keystatic's API handler.
 *
 * Problem: @keystatic/astro's makeHandler accesses `context.locals.runtime.env`
 * to read env vars. But @astrojs/cloudflare v13 removed that property and
 * replaced it with a Proxy trap that THROWS when accessed. This crash happens
 * before Keystatic checks the config-provided values.
 *
 * Solution: Wrap the Astro context with a Proxy that intercepts `locals`
 * access and returns a safe object with `runtime.env` populated from
 * the new `cloudflare:workers` module.
 */
export const ALL = async (context: APIContext) => {
	const { env } = await import('cloudflare:workers');

	// Build a safe locals object with runtime.env injected
	const safeLocals = {
		runtime: { env },
	};

	// Proxy the context to intercept locals access
	const safeContext = new Proxy(context, {
		get(target, prop, receiver) {
			if (prop === 'locals') return safeLocals;
			return Reflect.get(target, prop, receiver);
		},
	});

	const handler = makeHandler({ config: keystatic });
	return handler(safeContext as unknown as APIContext);
};
