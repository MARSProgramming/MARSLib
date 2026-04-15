#!/usr/bin/env python3
"""
MARSLib Performance Comparison Script

Compares current performance against established baselines and detects regressions.
Used in CI/CD pipeline to prevent performance degradation.
"""

import sys
import json
import argparse
from pathlib import Path


def load_baselines(baseline_file):
    """Load performance baselines from JSON file."""
    try:
        with open(baseline_file, 'r') as f:
            return json.load(f)
    except FileNotFoundError:
        print(f"Warning: Baseline file {baseline_file} not found. Creating new baseline.")
        return {}
    except json.JSONDecodeError as e:
        print(f"Error: Invalid JSON in baseline file: {e}")
        sys.exit(1)


def load_current_results(results_file):
    """Load current performance results from JSON file."""
    try:
        with open(results_file, 'r') as f:
            return json.load(f)
    except FileNotFoundError:
        print(f"Error: Results file {results_file} not found.")
        sys.exit(1)
    except json.JSONDecodeError as e:
        print(f"Error: Invalid JSON in results file: {e}")
        sys.exit(1)


def compare_performance(baselines, current, threshold=0.1):
    """
    Compare current performance against baselines.

    Args:
        baselines: Dictionary of baseline measurements
        current: Dictionary of current measurements
        threshold: Maximum allowed regression (default 10%)

    Returns:
        tuple: (has_regression, regressions, improvements)
    """
    regressions = []
    improvements = []

    for key, current_value in current.items():
        if key in baselines:
            baseline_value = baselines[key]
            ratio = current_value / baseline_value

            if ratio > (1.0 + threshold):
                regressions.append({
                    'metric': key,
                    'baseline': baseline_value,
                    'current': current_value,
                    'ratio': ratio,
                    'regression_percent': (ratio - 1.0) * 100
                })
            elif ratio < (1.0 - threshold):
                improvements.append({
                    'metric': key,
                    'baseline': baseline_value,
                    'current': current_value,
                    'ratio': ratio,
                    'improvement_percent': (1.0 - ratio) * 100
                })

    return len(regressions) > 0, regressions, improvements


def main():
    parser = argparse.ArgumentParser(description='Compare MARSLib performance against baselines')
    parser.add_argument('--baseline', default='config/performance/baselines.json',
                        help='Path to baseline JSON file')
    parser.add_argument('--results', required=True,
                        help='Path to current performance results JSON file')
    parser.add_argument('--threshold', type=float, default=0.1,
                        help='Regression threshold (default: 0.1 = 10%%)')
    parser.add_argument('--update-baseline', action='store_true',
                        help='Update baseline with current results')
    parser.add_argument('--verbose', '-v', action='store_true',
                        help='Print detailed comparison')

    args = parser.parse_args()

    # Load baselines and current results
    baselines = load_baselines(args.baseline)
    current = load_current_results(args.results)

    # Compare performance
    has_regression, regressions, improvements = compare_performance(
        baselines, current, args.threshold
    )

    # Print results
    print("Performance Comparison Results")
    print("=" * 50)

    if regressions:
        print(f"\n❌ PERFORMANCE REGRESSIONS DETECTED ({len(regressions)}):")
        for reg in regressions:
            print(f"  • {reg['metric']}:")
            print(f"    Baseline: {reg['baseline']:.2f} µs")
            print(f"    Current:  {reg['current']:.2f} µs")
            print(f"    Regression: {reg['regression_percent']:.1f}%%")

    if improvements:
        print(f"\n✅ PERFORMANCE IMPROVEMENTS ({len(improvements)}):")
        for imp in improvements:
            print(f"  • {imp['metric']}:")
            print(f"    Baseline: {imp['baseline']:.2f} µs")
            print(f"    Current:  {imp['current']:.2f} µs")
            print(f"    Improvement: {imp['improvement_percent']:.1f}%%")

    if not regressions and not improvements:
        print("\n✅ All performance metrics within acceptable range")

    if args.verbose:
        print("\nDetailed Comparison:")
        print("-" * 50)
        for key in sorted(set(list(baselines.keys()) + list(current.keys()))):
            baseline_val = baselines.get(key, 0)
            current_val = current.get(key, 0)
            if baseline_val > 0 and current_val > 0:
                ratio = current_val / baseline_val
                status = "✓" if 0.9 <= ratio <= 1.1 else "✗"
                print(f"  {status} {key}: {baseline_val:.2f} µs → {current_val:.2f} µs ({ratio:.2f}x)")

    # Update baseline if requested
    if args.update_baseline:
        baseline_path = Path(args.baseline)
        baseline_path.parent.mkdir(parents=True, exist_ok=True)
        with open(baseline_path, 'w') as f:
            json.dump(current, f, indent=2)
        print(f"\n✅ Baseline updated: {args.baseline}")

    # Exit with error if regression detected
    if has_regression:
        print(f"\n❌ Performance regression detected! Threshold: {args.threshold * 100:.0f}%")
        sys.exit(1)
    else:
        print(f"\n✅ No performance regression detected. Threshold: {args.threshold * 100:.0f}%")
        sys.exit(0)


if __name__ == '__main__':
    main()