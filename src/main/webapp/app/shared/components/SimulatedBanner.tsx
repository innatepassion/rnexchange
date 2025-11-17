/**
 * M6 Phase 2: SimulatedBanner Component
 *
 * A persistent banner component that displays "SIMULATED / NOT REAL MONEY"
 * to clearly indicate that RNExchange is a simulated trading environment.
 *
 * This component is used across Trader and Broker views to ensure educational
 * transparency and compliance with the RNExchange constitution's requirement
 * for clear disclaimers about the simulated nature of the platform.
 *
 * @module SimulatedBanner
 */

import React from 'react';
import { Alert } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faExclamationTriangle } from '@fortawesome/free-solid-svg-icons';

export interface ISimulatedBannerProps {
  /** Optional custom className for styling */
  className?: string;
  /** Optional variant (default: 'warning') */
  variant?: 'warning' | 'danger' | 'info';
  /** Whether to show the icon (default: true) */
  showIcon?: boolean;
  /** Optional custom message (default: "SIMULATED / NOT REAL MONEY") */
  message?: string;
}

/**
 * SimulatedBanner Component
 *
 * Renders a persistent banner warning that the trading environment is simulated.
 *
 * @param props - Component props
 * @returns React component
 *
 * @example
 * ```tsx
 * <SimulatedBanner />
 *
 * <SimulatedBanner variant="danger" className="my-custom-class" />
 * ```
 */
export const SimulatedBanner: React.FC<ISimulatedBannerProps> = ({
  className = '',
  variant = 'warning',
  showIcon = true,
  message = 'SIMULATED / NOT REAL MONEY',
}) => {
  return (
    <Alert
      color={variant}
      className={`simulated-banner mb-0 ${className}`}
      style={{
        borderRadius: 0,
        borderLeft: 'none',
        borderRight: 'none',
        fontWeight: 'bold',
        textAlign: 'center',
        padding: '0.75rem 1rem',
      }}
      data-cy="simulated-banner"
    >
      {showIcon && <FontAwesomeIcon icon={faExclamationTriangle} className="me-2" />}
      {message}
    </Alert>
  );
};

export default SimulatedBanner;
