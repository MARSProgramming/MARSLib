import { config, fields, collection } from '@keystatic/core';
import { block, wrapper } from '@keystatic/core/content-components';
import React from 'react';

const simBlock = (label: string) => block({
	label,
	schema: {
		'client:only': fields.text({ label: 'Astro Client Directive', defaultValue: 'react' }),
	},
});

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
	head: fields.array(
		fields.object({
			tag: fields.text({ label: 'HTML Tag' }),
			content: fields.text({ label: 'Content', multiline: true }),
		}),
		{ label: 'Custom Head Tags', itemLabel: props => props.fields.tag.value }
	),
	body: fields.mdx({
		extension: 'mdx',
		components: {
			ArmKgSim: simBlock('Arm Kg Simulator'),
			AutoSim: simBlock('Auto Simulator'),
			ElevatorPidSim: simBlock('Elevator PID Simulator'),
			FaultSim: simBlock('Faults Simulator'),
			FlywheelKvSim: simBlock('Flywheel Kv Simulator'),
			PhysicsSim: simBlock('Physics Simulator'),
			PowerSheddingSim: simBlock('Power Shedding Simulator'),
			SotmSim: simBlock('SOTM Simulator'),
			StateMachineSim: simBlock('State Machine Simulator'),
			SwerveSim: simBlock('Swerve Simulator'),
			SysIdSim: simBlock('SysId Simulator'),
			VisionSim: simBlock('Vision Simulator'),
			ZeroAllocationSim: simBlock('Zero Allocation Simulator'),
			RuleSection: wrapper({
				label: 'Rule Section',
				schema: {
					num: fields.text({ label: 'Rule Number' }),
					title: fields.text({ label: 'Rule Title' }),
				},
			}),
			CodeComparison: wrapper({
				label: 'Code Comparison',
				schema: {},
			}),
			CodeViolation: wrapper({
				label: 'Code Violation',
				schema: {},
			}),
			CodeStandard: wrapper({
				label: 'Code Standard',
				schema: {},
			}),
			StandardHeader: block({
				label: 'Standard Header',
				schema: {},
			}),
			SplashContainer: wrapper({
				label: 'Splash Container',
				schema: {},
			}),
			HomeHero: block({
				label: 'Home Hero',
				schema: {},
			}),
			HomeSimulatorContainer: wrapper({
				label: 'Home Simulator Container',
				schema: {},
			}),
			HomeHallOfFame: block({
				label: 'Home Hall of Fame',
				schema: {},
			}),
			HomeTutorialGrid: block({
				label: 'Home Tutorial Grid',
				schema: {},
			}),
			SponsorsList: block({
				label: 'Sponsors List',
				schema: {},
			}),
		},
	}),
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
			path: 'website/src/content/docs/*',
			format: contentFormat,
			schema: starlightSchema,
		}),

		setup: collection({
			label: 'Setup & CI',
			slugField: 'title',
			path: 'website/src/content/docs/tutorials/setup/*',
			format: contentFormat,
			schema: starlightSchema,
		}),

		zeroToHero: collection({
			label: 'Zero → Hero',
			slugField: 'title',
			path: 'website/src/content/docs/tutorials/zero-to-hero/*',
			format: contentFormat,
			schema: starlightSchema,
		}),

		framework: collection({
			label: 'Framework Tutorials',
			slugField: 'title',
			path: 'website/src/content/docs/tutorials/framework/*',
			format: contentFormat,
			schema: starlightSchema,
		}),

		elite: collection({
			label: 'Elite Techniques',
			slugField: 'title',
			path: 'website/src/content/docs/tutorials/elite/*',
			format: contentFormat,
			schema: starlightSchema,
		}),
	},
});
