import React from 'react';
import { Translate } from 'react-jhipster';

import { NavItem, NavLink, NavbarBrand } from 'reactstrap';
import { NavLink as Link } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

export const BrandIcon = props => (
  <div {...props} className="brand-icon">
    <img
      src="content/images/rnexchange-logo.png"
      alt="RNExchange Logo"
      onError={e => {
        // Fallback to text logo if image doesn't exist
        e.currentTarget.style.display = 'none';
        const parent = e.currentTarget.parentElement;
        if (parent && !parent.querySelector('.rnexchange-text-logo')) {
          const textLogo = document.createElement('div');
          textLogo.className = 'rnexchange-text-logo';
          textLogo.innerHTML = '<span style="font-size: 1.5rem; font-weight: bold; color: #0066cc;">RNX</span>';
          textLogo.style.display = 'inline-block';
          parent.appendChild(textLogo);
        }
      }}
    />
  </div>
);

export const Brand = () => (
  <NavbarBrand tag={Link} to="/" className="brand-logo">
    <BrandIcon />
    <span className="brand-title">
      <Translate contentKey="global.title">Rnexchange</Translate>
    </span>
    <span className="navbar-version">{VERSION.toLowerCase().startsWith('v') ? VERSION : `v${VERSION}`}</span>
  </NavbarBrand>
);

export const Home = () => (
  <NavItem>
    <NavLink tag={Link} to="/" className="d-flex align-items-center">
      <FontAwesomeIcon icon="home" />
      <span>
        <Translate contentKey="global.menu.home">Home</Translate>
      </span>
    </NavLink>
  </NavItem>
);
