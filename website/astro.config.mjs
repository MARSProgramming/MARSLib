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
						const inject = `import { ArmKgSim, AutoSim, ElevatorPidSim, FaultSim, FlywheelKvSim, PhysicsSim, PowerSheddingSim, SotmSim, StateMachineSim, SwerveSim, SysIdSim, VisionSim, ZeroAllocationSim, RuleSection, CodeComparison, CodeViolation, CodeStandard, StandardHeader, SplashContainer, HomeHero, HomeSimulatorContainer, HomeHallOfFame, HomeTutorialGrid, SponsorsList } from '/src/components/index.ts';\n\n`;
						
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
			],
			logo: {
				src: './src/assets/mars-logo.png',
			},
			social: [{ icon: 'github', label: 'GitHub', href: 'https://github.com/MARSProgramming/MARSLib' }],
			sidebar: [
				{
					label: 'Tutorials',
					autogenerate: { directory: 'tutorials' },
				},
				{
					label: 'Core Standards',
					link: '/standards/',
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
