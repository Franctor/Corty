import { Chart, Plugin } from 'chart.js';

function resolveCssVar(value: string): string {
  const match = value.match(/^var\((--[\w-]+)\)$/);
  if (!match) return value;
  return getComputedStyle(document.documentElement).getPropertyValue(match[1]).trim() || value;
}

function applyThemeToScales(chart: Chart): void {
  const scales = chart.options.scales as Record<string, Record<string, unknown>> | undefined;
  if (!scales) return;

  for (const key of Object.keys(scales)) {
    const scale = scales[key];
    if (!scale) continue;

    const ticks = scale['ticks'] as Record<string, unknown> | undefined;
    if (ticks?.['color'] && typeof ticks['color'] === 'string') {
      ticks['color'] = resolveCssVar(ticks['color']);
    }

    const grid = scale['grid'] as Record<string, unknown> | undefined;
    if (grid?.['color'] && typeof grid['color'] === 'string') {
      grid['color'] = resolveCssVar(grid['color']);
    }
  }
}

export const chartThemePlugin: Plugin = {
  id: 'cortyChartTheme',
  beforeInit: applyThemeToScales,
  beforeUpdate: applyThemeToScales,
};

Chart.register(chartThemePlugin);
