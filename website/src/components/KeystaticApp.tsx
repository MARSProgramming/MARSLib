import '@keystatic/astro/ui';
import '@keystatic/astro/api';
import { makePage } from '@keystatic/astro/ui';
import config from '../../keystatic.config';

export const KeystaticApp = makePage(config);
