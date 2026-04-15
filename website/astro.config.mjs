// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';
import react from '@astrojs/react';
import markdoc from '@astrojs/markdoc';
import cloudflare from '@astrojs/cloudflare';

// https://astro.build/config
export default defineConfig({
	site: 'https://marslib.pages.dev',
	output: 'server',
	adapter: cloudflare({
		imageService: 'cloudflare',
		routes: {
			extend: {
				include: ['/keystatic', '/keystatic/*', '/api/keystatic', '/api/keystatic/*']
			}
		}
	}),
	vite: {
		ssr: {
			external: ['node:path', 'node:fs', 'node:url', 'node:util', 'path', 'fs', 'url', 'util', 'postcss', 'util-deprecate'],
		},
		build: {
			chunkSizeWarningLimit: 2000,
		},
		plugins: [
			{
				name: 'auto-inject-mdx-components',
				enforce: 'pre',
				transform(code, id) {
					if (id.endsWith('.mdx') && !id.includes('node_modules')) {
						const inject = `import { ArmKgSim, AutoSim, ElevatorPidSim, FaultSim, FlywheelKvSim, PhysicsSim, PowerSheddingSim, SotmSim, StateMachineSim, SwerveSim, SysIdSim, VisionSim, ZeroAllocationSim, RuleSection, CodeComparison, CodeViolation, CodeStandard, StandardHeader, SplashContainer, HomeHero, HomeSimulatorContainer, HomeHallOfFame, HomeTutorialGrid, SponsorsList } from '/src/components/index.ts';\nimport Mermaid from '/src/components/Mermaid.astro';\n\n`;
						
						// Inject after frontmatter
						const fmEndIndex = code.indexOf('---', 3);
						if (fmEndIndex !== -1 && code.startsWith('---')) {
							return code.slice(0, fmEndIndex + 3) + '\n' + inject + code.slice(fmEndIndex + 3);
						}
						
						return inject + code;
					}
				}
			}
		]
	},
	integrations: [
		starlight({
			title: 'MARSLib Documentation',
			customCss: [
				'./src/styles/custom.css',
				'./src/styles/copy-button.css',
			],
			head: [
				{
					tag: 'meta',
					attrs: { name: 'color-scheme', content: 'dark only' },
				},
				{
					tag: 'script',
					content: "if (typeof document !== 'undefined') { document.documentElement.dataset.theme = 'dark'; new MutationObserver(function() { if (document.documentElement.dataset.theme !== 'dark') { document.documentElement.dataset.theme = 'dark'; } }).observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme'] }); }"
				},
				{
					tag: 'script',
					attrs: { src: '/src/scripts/copy-button.js', defer: true },
				},
				{
					tag: 'script',
					attrs: { src: '/src/scripts/search-shortcut.js', defer: true },
				},
			],
			logo: {
				src: './src/assets/mars-logo.png',
			},
			favicon: '/img/mars-logo.png',
			social: [{ icon: 'github', label: 'GitHub', href: 'https://github.com/MARSProgramming/MARSLib' }],
			sidebar: [
				{
					label: 'Tutorials',
					items: [
						{ label: 'Zero-to-Hero (Start Here)', autogenerate: { directory: 'tutorials/zero-to-hero' } },
						{ label: 'Setup & Infrastructure', autogenerate: { directory: 'tutorials/setup' } },
						{ label: 'Framework Architecture', autogenerate: { directory: 'tutorials/framework' } },
						{ label: 'Elite-Level Workflows', autogenerate: { directory: 'tutorials/elite' } }
					]
				},
				{
					label: 'Core Standards',
					link: '/standards/',
				},
				{
					label: 'Accessibility Commitment',
					link: '/accessibility/',
				},
				{
					label: 'API Reference',
					link: 'https://MARSProgramming.github.io/MARSLib/javadoc/index.html',
					attrs: { target: '_blank' }
				},
			],
			components: {
				Footer: './src/components/Footer.astro',
				SiteTitle: './src/components/SiteTitle.astro',
				PageTitle: './src/components/PageTitle.astro',
			},
		}),
		react(),
		markdoc()
	],
});
