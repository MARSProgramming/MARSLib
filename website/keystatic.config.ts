import { config, fields, collection, singleton } from '@keystatic/core';
import React from 'react';

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
			Reference: ['guides', 'reference'],
		},
	},
	storage: {
		kind: 'github',
		repo: 'MARSProgramming/MARSLib',
	},
	collections: {
		// ── Top-Level Pages ────────────────────────────────────────
		pages: collection({
			label: 'Site Pages',
			slugField: 'title',
			path: 'website/src/content/docs/*',
			format: { contentField: 'content' },
			schema: {
				title: fields.slug({ name: { label: 'Title' } }),
				sidebar: fields.object(
					{
						order: fields.integer({
							label: 'Sidebar Order',
						}),
					},
					{ label: 'Sidebar Settings' }
				),
				content: fields.mdx({ label: 'Content' }),
			},
		}),

		// ── Setup & CI Tutorials ──────────────────────────────────
		setup: collection({
			label: 'Setup & CI',
			slugField: 'title',
			path: 'website/src/content/docs/tutorials/setup/*',
			format: { contentField: 'content' },
			schema: {
				title: fields.slug({ name: { label: 'Title' } }),
				sidebar: fields.object(
					{
						order: fields.integer({
							label: 'Sidebar Order',
						}),
					},
					{ label: 'Sidebar Settings' }
				),
				content: fields.mdx({ label: 'Content' }),
			},
		}),

		// ── Zero-to-Hero Tutorial Series ──────────────────────────
		zeroToHero: collection({
			label: 'Zero → Hero',
			slugField: 'title',
			path: 'website/src/content/docs/tutorials/zero-to-hero/*',
			format: { contentField: 'content' },
			schema: {
				title: fields.slug({ name: { label: 'Title' } }),
				sidebar: fields.object(
					{
						order: fields.integer({
							label: 'Sidebar Order',
						}),
					},
					{ label: 'Sidebar Settings' }
				),
				content: fields.mdx({ label: 'Content' }),
			},
		}),

		// ── Framework Deep-Dive Tutorials ─────────────────────────
		framework: collection({
			label: 'Framework Tutorials',
			slugField: 'title',
			path: 'website/src/content/docs/tutorials/framework/*',
			format: { contentField: 'content' },
			schema: {
				title: fields.slug({ name: { label: 'Title' } }),
				sidebar: fields.object(
					{
						order: fields.integer({
							label: 'Sidebar Order',
						}),
					},
					{ label: 'Sidebar Settings' }
				),
				content: fields.mdx({ label: 'Content' }),
			},
		}),

		// ── Elite / Advanced Tutorials ─────────────────────────────
		elite: collection({
			label: 'Elite Techniques',
			slugField: 'title',
			path: 'website/src/content/docs/tutorials/elite/*',
			format: { contentField: 'content' },
			schema: {
				title: fields.slug({ name: { label: 'Title' } }),
				sidebar: fields.object(
					{
						order: fields.integer({
							label: 'Sidebar Order',
						}),
					},
					{ label: 'Sidebar Settings' }
				),
				content: fields.mdx({ label: 'Content' }),
			},
		}),

		// ── Guides ─────────────────────────────────────────────────
		guides: collection({
			label: 'Guides',
			slugField: 'title',
			path: 'website/src/content/docs/guides/*',
			format: { contentField: 'content' },
			schema: {
				title: fields.slug({ name: { label: 'Title' } }),
				sidebar: fields.object(
					{
						order: fields.integer({
							label: 'Sidebar Order',
						}),
					},
					{ label: 'Sidebar Settings' }
				),
				content: fields.mdx({ label: 'Content' }),
			},
		}),

		// ── API Reference Pages ────────────────────────────────────
		reference: collection({
			label: 'Reference',
			slugField: 'title',
			path: 'website/src/content/docs/reference/*',
			format: { contentField: 'content' },
			schema: {
				title: fields.slug({ name: { label: 'Title' } }),
				sidebar: fields.object(
					{
						order: fields.integer({
							label: 'Sidebar Order',
						}),
					},
					{ label: 'Sidebar Settings' }
				),
				content: fields.mdx({ label: 'Content' }),
			},
		}),
	},
});
