#!/usr/bin/env bash
#
# Execute JSL performance comparison tests.
#
# Usage:
#   ./execute-performance-tests.sh [options]
#
# Options:
#   --module MODULE       Module to test: jsl2psm (default), jsl2ui, all
#   --iterations N        Number of ZETA iterations per model (default: 3)
#   --models-dir PATH     Additional directory to discover .jsl models
#   --no-warmup           Disable warmup phase
#   --comparison-mode M   ModelComparator mode: STRICT (default), STRUCTURAL, LENIENT
#   -h, --help            Show this help
#

set -euo pipefail

MODULE=jsl2psm
ITERATIONS=3
WARMUP=true
COMPARISON_MODE=STRICT
MODELS_DIR=""

while [[ $# -gt 0 ]]; do
    case "$1" in
        --module)
            MODULE="$2"
            shift 2
            ;;
        --iterations)
            ITERATIONS="$2"
            shift 2
            ;;
        --models-dir)
            MODELS_DIR="$2"
            shift 2
            ;;
        --no-warmup)
            WARMUP=false
            shift
            ;;
        --comparison-mode)
            COMPARISON_MODE="$2"
            shift 2
            ;;
        -h|--help)
            head -16 "$0" | tail -13
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Validate module
case "$MODULE" in
    jsl2psm|jsl2ui|all) ;;
    *)
        echo "Invalid module: $MODULE (must be jsl2psm, jsl2ui, or all)"
        exit 1
        ;;
esac

run_module() {
    local module_name="$1"
    local maven_module="$2"
    local test_class="$3"

    echo "=== ${module_name} Performance Tests ==="
    echo "  Iterations:      $ITERATIONS"
    echo "  Warmup:          $WARMUP"
    echo "  Comparison mode: $COMPARISON_MODE"
    [ -n "$MODELS_DIR" ] && echo "  Models dir:      $MODELS_DIR"
    echo ""

    MVN_ARGS=(
        test
        -Pperformance
        -pl "$maven_module"
        "-Dtest=$test_class"
        "-Djudo.test.zeta.iterations=$ITERATIONS"
        "-Djudo.test.warmup=$WARMUP"
        "-Djudo.test.comparison.mode=$COMPARISON_MODE"
    )

    if [ -n "$MODELS_DIR" ]; then
        MVN_ARGS+=("-Djudo.test.discovery.basedir=$MODELS_DIR")
    fi

    ./mvnw "${MVN_ARGS[@]}"

    local results_file="${maven_module}/target/comparison-results.json"
    if [ -f "$results_file" ]; then
        echo ""
        echo "Results written to: $results_file"
    fi
}

case "$MODULE" in
    jsl2psm)
        run_module "JSL2PSM" "judo-tatami-jsl-jsl2psm" "Jsl2PsmDiscoveryComparisonTest"
        ;;
    jsl2ui)
        run_module "JSL2UI" "judo-tatami-jsl-jsl2ui" "Jsl2UiDiscoveryComparisonTest"
        ;;
    all)
        run_module "JSL2PSM" "judo-tatami-jsl-jsl2psm" "Jsl2PsmDiscoveryComparisonTest"
        echo ""
        echo "================================================================"
        echo ""
        run_module "JSL2UI" "judo-tatami-jsl-jsl2ui" "Jsl2UiDiscoveryComparisonTest"
        ;;
esac
