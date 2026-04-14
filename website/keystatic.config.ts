import { config, fields, collection } from '@keystatic/core';
import React from 'react';

/**
 * Shared Starlight frontmatter schema.
 *
 * Uses fields.emptyContent() to tell Keystatic these are .mdx content files
 * without trying to parse the MDX body. The tutorials use JSX imports and
 * raw HTML that Keystatic's structured MDX editor cannot handle.
 * Content editing is done in VS Code; the CMS manages metadata only.
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
	body: fields.emptyContent({ extension: 'mdx' }),
};

const contentFormat = { contentField: 'body' } as const;

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
			entryLayout: 'form',
			path: 'website/src/content/docs/*',
			format: contentFormat,
			schema: starlightSchema,
		}),

		setup: collection({
			label: 'Setup & CI',
			slugField: 'title',
			entryLayout: 'form',
			path: 'website/src/content/docs/tutorials/setup/*',
			format: contentFormat,
			schema: starlightSchema,
		}),

		zeroToHero: collection({
			label: 'Zero → Hero',
			slugField: 'title',
			entryLayout: 'form',
			path: 'website/src/content/docs/tutorials/zero-to-hero/*',
			format: contentFormat,
			schema: starlightSchema,
		}),

		framework: collection({
			label: 'Framework Tutorials',
			slugField: 'title',
			entryLayout: 'form',
			path: 'website/src/content/docs/tutorials/framework/*',
			format: contentFormat,
			schema: starlightSchema,
		}),

		elite: collection({
			label: 'Elite Techniques',
			slugField: 'title',
			entryLayout: 'form',
			path: 'website/src/content/docs/tutorials/elite/*',
			format: contentFormat,
			schema: starlightSchema,
		}),
	},
});
