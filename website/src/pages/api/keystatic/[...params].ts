import { makeAPIRoute } from '@keystatic/astro/api';
import keystaticConfig from '../../../../keystatic.config';

export const all = makeAPIRoute(keystaticConfig);