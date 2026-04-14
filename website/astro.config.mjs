// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';
import react from '@astrojs/react';
import markdoc from '@astrojs/markdoc';
import keystatic from '@keystatic/astro';
import cloudflare from '@astrojs/cloudflare';

// https://astro.build/config
export default defineConfig({
	site: 'https://marslib.pages.dev',
	output: 'static',
	adapter: cloudflare({
		imageService: 'cloudflare'
	}),
	vite: {
		ssr: {
			external: ['node:path', 'node:fs', 'node:url', 'node:util', 'path', 'fs', 'url', 'util', 'postcss', 'util-deprecate'],
		},
		build: {
			chunkSizeWarningLimit: 2000,
		},
	},
	integrations: [
		starlight({
			title: 'My Docs',
			customCss: [
				'./src/styles/custom.css',
			],
			social: [{ icon: 'github', label: 'GitHub', href: 'https://github.com/withastro/starlight' }],
			sidebar: [
				{
					label: 'Guides',
					items: [
						// Each item here is one entry in the navigation menu.
						{ label: 'Example Guide', slug: 'guides/example' },
					],
				},
				{
					label: 'Reference',
					autogenerate: { directory: 'reference' },
				},
			],
		}),
		react(),
		markdoc(),
		keystatic()
	],
});
