/**
 * Acceptance tests for the Informatics platform.
 * 
 * These tests boot up the full Spring application context with embedded infrastructure
 * (H2 database, embedded Kafka) and mock judge integration to verify end-to-end
 * functionality including:
 * 
 * - User registration and authentication
 * - Contest creation and lifecycle management
 * - Submission processing and scoring
 * - Standings integrity verification
 * - User profile metadata (solved/failed problems)
 * - Upsolving functionality
 */
package ge.freeuni.informatics.acceptance;

