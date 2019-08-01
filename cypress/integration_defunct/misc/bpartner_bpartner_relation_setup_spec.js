<<<<<<< HEAD:cypress/integration_defunct/misc/bpartner_bpartner_relation_setup_spec.js
import { BPartner } from '../../support/utils/bpartner_ui';
=======
import { BPartner } from '../../support/utils/bpartner';
import { PriceList } from '../../support/utils/pricelist';
import { DiscountSchema } from '../../support/utils/discountschema';
>>>>>>> 0df9d44... - fixing bpartner relation test #106:cypress/integration/misc/bpartner_bpartner_relation_spec.js

describe('BPartner relations', function() {
  const timestamp = new Date().getTime(); // used in the document names, for ordering
  const customer1Name = `Customer1 ${timestamp}`;
  const customer2Name = `Customer2 ${timestamp}`;
  // const timestamp = new Date().getTime();
  const priceListName = `Test Pricelist ${timestamp}`;
  const discountSchemaName = `DiscountSchemaTest ${timestamp}`;

  before(function() {
    cy.fixture('discount/discountschema.json').then(discountschemaJson => {
      Object.assign(new DiscountSchema(), discountschemaJson)
        .setName(discountSchemaName)
        .apply();
    });

    cy.fixture('price/pricelist.json').then(pricelistJson => {
      Object.assign(new PriceList(priceListName), pricelistJson).apply();
    });

    cy.fixture('sales/simple_customer.json').then(customerJson => {
      Object.assign(new BPartner(), customerJson)
        // .setCustomer(true)
        .setCustomerDiscountSchema(discountSchemaName)
        .setCustomerPricingSystem(priceListName)
        .setName(customer1Name)
        .clearContacts()
        .apply();
    });

    cy.fixture('sales/simple_customer.json').then(customerJson => {
      Object.assign(new BPartner(), customerJson)
        .setName(customer2Name)
        // .setCustomer(true)
        .setCustomerDiscountSchema(discountSchemaName)
        .setCustomerPricingSystem(priceListName)
        .clearContacts()
        .apply();
    });
  });

  //create bpartner relation
  it('Create a bpartner relation', function() {
    cy.executeHeaderActionWithDialog('CreateBPRelationFromDocument');
    cy.selectInListField('C_BPartner_Location_ID', 'Address1', true /*modal*/, '/rest/api/process/.*' /*rewriteUrl*/);
    cy.writeIntoLookupListField(
      'C_BPartnerRelation_ID',
      customer2Name,
      customer2Name,
      false /*typeList*/,
      true /*modal*/,
      '/rest/api/process/.*' /*rewriteUrl*/
    );
    cy.selectInListField(
      'C_BPartnerRelation_Location_ID',
      'Address1',
      true /*modal*/,
      '/rest/api/process/.*' /*rewriteUrl*/
    );

    cy.getCheckboxValue('IsBillTo');
    cy.clickOnCheckBox('IsShipTo', 'checked', true /*modal*/, '/rest/api/process/.*' /*rewriteUrl*/);
    cy.pressStartButton();
  });
});
