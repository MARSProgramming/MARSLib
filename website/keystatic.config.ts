import { config, fields, collection } from '@keystatic/core';
import { block, wrapper, mark } from '@keystatic/core/content-components';
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
			CodePlayground: block({
				label: 'Code Playground',
				schema: {},
			}),
			FieldVisualizer: block({
				label: 'Field Visualizer',
				schema: {},
			}),
			PerformanceDashboard: block({
				label: 'Performance Dashboard',
				schema: {},
			}),
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
			span: mark({
				label: 'Span',
				icon: React.createElement('span', null, 'S'),
				tag: 'span',
				schema: {
					class: fields.text({ label: 'Class', defaultValue: 'mars-num' }),
				},
				className: (props) => props.value.class,
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
			HomeCoreValues: block({
				label: 'Home Core Values',
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
			CoreValueCallout: wrapper({
				label: 'Core Value Callout',
				schema: {
					value: fields.select({
						label: 'Value',
						options: [
							{ label: 'Discovery', value: 'discovery' },
							{ label: 'Innovation', value: 'innovation' },
							{ label: 'Impact', value: 'impact' },
							{ label: 'Teamwork', value: 'teamwork' },
							{ label: 'Inclusion', value: 'inclusion' },
							{ label: 'Fun', value: 'fun' },
						],
						defaultValue: 'discovery',
					}),
				},
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
					src: '/img/mars-logo.png',
					height: 28,
					alt: 'MARSLib Logo',
				}),
		},
		navigation: {
			'Home & Top-Level': ['pages'],
			'Getting Started': ['gettingStarted'],
			'Framework Architecture': ['frameworkArchitecture'],
			'Robot Subsystems': ['subsystems'],
			'Advanced Workflows': ['advanced'],
			'Operations & Deploy': ['operations'],
			Troubleshooting: ['troubleshooting'],
			'Interactive Learning': ['interactive', 'visualDiagrams'],
			Reference: ['reference'],
			'Community & Culture': ['contributing'],
		},
	},
	storage: {
		kind: 'github',
		repo: 'MARSProgramming/MARSLib',
	},
	collections: {
		pages: collection({
			label: 'Top-Level Pages',
			slugField: 'title',
			path: 'website/src/content/docs/*',
			format: contentFormat,
			schema: starlightSchema,
		}),

		gettingStarted: collection({
			label: 'Getting Started',
			slugField: 'title',
			path: 'website/src/content/docs/getting-started/*',
			format: contentFormat,
			schema: starlightSchema,
		}),

		frameworkArchitecture: collection({
			label: 'Framework Architecture',
			slugField: 'title',
			path: 'website/src/content/docs/framework-architecture/*',
			format: contentFormat,
			schema: starlightSchema,
		}),

		subsystems: collection({
			label: 'Robot Subsystems',
			slugField: 'title',
			path: 'website/src/content/docs/subsystems/*',
			format: contentFormat,
			schema: starlightSchema,
		}),

		advanced: collection({
			label: 'Advanced Workflows',
			slugField: 'title',
			path: 'website/src/content/docs/advanced/*',
			format: contentFormat,
			schema: starlightSchema,
		}),

		operations: collection({
			label: 'Operations & Deploy',
			slugField: 'title',
			path: 'website/src/content/docs/operations/*',
			format: contentFormat,
			schema: starlightSchema,
		}),

		troubleshooting: collection({
			label: 'Troubleshooting',
			slugField: 'title',
			path: 'website/src/content/docs/troubleshooting/*',
			format: contentFormat,
			schema: starlightSchema,
		}),

		interactive: collection({
			label: 'Interactive Exercises',
			slugField: 'title',
			path: 'website/src/content/docs/interactive/**/*',
			format: contentFormat,
			schema: starlightSchema,
		}),

		visualDiagrams: collection({
			label: 'Visual Diagrams',
			slugField: 'title',
			path: 'website/src/content/docs/visual-diagrams/*',
			format: contentFormat,
			schema: starlightSchema,
		}),

		reference: collection({
			label: 'Reference',
			slugField: 'title',
			path: 'website/src/content/docs/reference/*',
			format: contentFormat,
			schema: starlightSchema,
		}),

		contributing: collection({
			label: 'Community & Culture',
			slugField: 'title',
			path: 'website/src/content/docs/contributing/*',
			format: contentFormat,
			schema: starlightSchema,
		}),
	},
});
