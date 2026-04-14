import { config, fields, collection } from '@keystatic/core';
import React from 'react';

/**
 * Shared schema matching Starlight's frontmatter fields.
 *
 * NOTE: contentField is intentionally omitted. The tutorial MDX files use
 * JSX imports and custom React components (AutoSim, etc.) that Keystatic's
 * built-in MDX parser cannot handle. The CMS manages frontmatter/metadata
 * only — content is edited in VS Code where full JSX support is available.
 */
const starlightSchema = {
	title: fields.slug({ name: { label: 'Title' } }),
	description: fields.text({ label: 'Description', multiline: true }),
	id: fields.text({ label: 'Page ID' }),
	template: fields.select({
		label: 'Template',
		options: [
			{ label: 'Doc (default)', value: 'doc' },
			{ label: 'Splash', value: 'splash' },
		],
		defaultValue: 'doc',
	}),
	sidebar: fields.object(
		{
			order: fields.integer({ label: 'Sidebar Order' }),
			label: fields.text({ label: 'Sidebar Label' }),
		},
		{ label: 'Sidebar Settings' }
	),
	head: fields.text({ label: 'Custom Head Tag (raw)', multiline: true }),
};

export default config({
	ui: {
		brand: {
			name: 'MARSLib CMS',
			mark: () =>
				React.createElement('img', {
					src: '/mars-logo.png',
					height: 28,
					alt: 'MARSLib Logo',
				}),
		},
		navigation: {
			'Getting Started': ['pages', 'setup'],
			Tutorials: ['zeroToHero', 'framework', 'elite'],
		},
	},
	storage: {
		kind: 'github',
		repo: 'MARSProgramming/MARSLib',
	},
	collections: {
		pages: collection({
			label: 'Site Pages',
			slugField: 'title',
			path: 'website/src/content/docs/*',
			schema: starlightSchema,
		}),

		setup: collection({
			label: 'Setup & CI',
			slugField: 'title',
			path: 'website/src/content/docs/tutorials/setup/*',
			schema: starlightSchema,
		}),

		zeroToHero: collection({
			label: 'Zero → Hero',
			slugField: 'title',
			path: 'website/src/content/docs/tutorials/zero-to-hero/*',
			schema: starlightSchema,
		}),

		framework: collection({
			label: 'Framework Tutorials',
			slugField: 'title',
			path: 'website/src/content/docs/tutorials/framework/*',
			schema: starlightSchema,
		}),

		elite: collection({
			label: 'Elite Techniques',
			slugField: 'title',
			path: 'website/src/content/docs/tutorials/elite/*',
			schema: starlightSchema,
		}),
	},
});
