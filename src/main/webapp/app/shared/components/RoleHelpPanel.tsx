import React, { useState } from 'react';
import { Translate } from 'react-jhipster';
import { AUTHORITIES } from 'app/config/constants';
import { useAppSelector } from 'app/config/store';
import { hasAnyAuthority } from 'app/shared/auth/authorities';
import './RoleHelpPanel.scss';

/**
 * M6 User Story 5 (T052): Shared role-aware help panel component.
 * Reads content from i18n JSON files (trader-help.json, broker-help.json, exchange-help.json).
 */
const RoleHelpPanel: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const account = useAppSelector(state => state.authentication.account);
  const authorities = account?.authorities || [];

  const isTrader = hasAnyAuthority(authorities, [AUTHORITIES.TRADER]);
  const isBrokerAdmin = hasAnyAuthority(authorities, [AUTHORITIES.BROKER_ADMIN]);
  const isExchangeOperator = hasAnyAuthority(authorities, [AUTHORITIES.EXCHANGE_OPERATOR]);

  // Determine which help content to show based on role
  const helpKey = isTrader ? 'trader-help' : isBrokerAdmin ? 'broker-help' : isExchangeOperator ? 'exchange-help' : null;

  if (!helpKey) {
    return null; // No help available for this role
  }

  return (
    <div className="role-help-panel" data-cy="role-help-panel">
      <button className="help-toggle-btn" onClick={() => setIsOpen(!isOpen)} aria-expanded={isOpen} data-cy="help-toggle-button">
        <span className="help-icon">?</span>
        <Translate contentKey={`${helpKey}.title`}>How to use RNExchange</Translate>
      </button>

      {isOpen && (
        <div className="help-content" data-cy="help-content">
          <div className="help-header">
            <h3>
              <Translate contentKey={`${helpKey}.title`}>How to use RNExchange</Translate>
            </h3>
            <button className="close-btn" onClick={() => setIsOpen(false)} aria-label="Close help">
              ×
            </button>
          </div>

          <div className="help-body">
            <p className="help-intro">
              <Translate contentKey={`${helpKey}.introduction`}>Introduction text</Translate>
            </p>

            <section className="help-section">
              <h4>
                <Translate contentKey={`${helpKey}.responsibilities.title`}>Your Responsibilities</Translate>
              </h4>
              <ul>
                {[1, 2, 3, 4, 5].map(i => (
                  <li key={i}>
                    <Translate contentKey={`${helpKey}.responsibilities.items.${i - 1}`}>Responsibility {i}</Translate>
                  </li>
                ))}
              </ul>
            </section>

            <section className="help-section">
              <h4>
                <Translate contentKey={`${helpKey}.screens.title`}>Main Screens</Translate>
              </h4>
              <ul>
                {[0, 1, 2, 3].map(i => (
                  <li key={i}>
                    <strong>
                      <Translate contentKey={`${helpKey}.screens.items.${i}.name`}>Screen name</Translate>
                    </strong>
                    : <Translate contentKey={`${helpKey}.screens.items.${i}.description`}>Screen description</Translate>
                  </li>
                ))}
              </ul>
            </section>

            <section className="help-section">
              <h4>
                <Translate contentKey={`${helpKey}.flows.title`}>Key Flows</Translate>
              </h4>
              <ol>
                {[1, 2, 3, 4, 5, 6].map(step => (
                  <li key={step}>
                    <strong>
                      <Translate contentKey={`${helpKey}.flows.items.${step - 1}.title`}>Step {step} title</Translate>
                    </strong>
                    : <Translate contentKey={`${helpKey}.flows.items.${step - 1}.description`}>Step {step} description</Translate>
                  </li>
                ))}
              </ol>
            </section>

            <section className="help-section">
              <h4>
                <Translate contentKey={`${helpKey}.demoUsers.title`}>Demo Users</Translate>
              </h4>
              <p>
                <Translate contentKey={`${helpKey}.demoUsers.description`}>Demo user description</Translate>
              </p>
            </section>

            <div className="help-disclaimer">
              <strong>
                <Translate contentKey={`${helpKey}.disclaimer`}>Disclaimer</Translate>
              </strong>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default RoleHelpPanel;
