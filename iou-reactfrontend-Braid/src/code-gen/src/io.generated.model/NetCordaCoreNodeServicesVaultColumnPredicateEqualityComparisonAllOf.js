/**
 * 
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 *
 */

import ApiClient from '../ApiClient';

/**
 * The NetCordaCoreNodeServicesVaultColumnPredicateEqualityComparisonAllOf model module.
 * @module io.generated.model/NetCordaCoreNodeServicesVaultColumnPredicateEqualityComparisonAllOf
 * @version 1.0.0
 */
class NetCordaCoreNodeServicesVaultColumnPredicateEqualityComparisonAllOf {
    /**
     * Constructs a new <code>NetCordaCoreNodeServicesVaultColumnPredicateEqualityComparisonAllOf</code>.
     * @alias module:io.generated.model/NetCordaCoreNodeServicesVaultColumnPredicateEqualityComparisonAllOf
     */
    constructor() { 
        
        NetCordaCoreNodeServicesVaultColumnPredicateEqualityComparisonAllOf.initialize(this);
    }

    /**
     * Initializes the fields of this object.
     * This method is used by the constructors of any subclasses, in order to implement multiple inheritance (mix-ins).
     * Only for internal use.
     */
    static initialize(obj) { 
    }

    /**
     * Constructs a <code>NetCordaCoreNodeServicesVaultColumnPredicateEqualityComparisonAllOf</code> from a plain JavaScript object, optionally creating a new instance.
     * Copies all relevant properties from <code>data</code> to <code>obj</code> if supplied or a new instance if not.
     * @param {Object} data The plain JavaScript object bearing properties of interest.
     * @param {module:io.generated.model/NetCordaCoreNodeServicesVaultColumnPredicateEqualityComparisonAllOf} obj Optional instance to populate.
     * @return {module:io.generated.model/NetCordaCoreNodeServicesVaultColumnPredicateEqualityComparisonAllOf} The populated <code>NetCordaCoreNodeServicesVaultColumnPredicateEqualityComparisonAllOf</code> instance.
     */
    static constructFromObject(data, obj) {
        if (data) {
            obj = obj || new NetCordaCoreNodeServicesVaultColumnPredicateEqualityComparisonAllOf();

            if (data.hasOwnProperty('operator')) {
                obj['operator'] = ApiClient.convertToType(data['operator'], 'String');
            }
            if (data.hasOwnProperty('rightLiteral')) {
                obj['rightLiteral'] = ApiClient.convertToType(data['rightLiteral'], Object);
            }
        }
        return obj;
    }


}

/**
 * @member {module:io.generated.model/NetCordaCoreNodeServicesVaultColumnPredicateEqualityComparisonAllOf.OperatorEnum} operator
 */
NetCordaCoreNodeServicesVaultColumnPredicateEqualityComparisonAllOf.prototype['operator'] = undefined;

/**
 * @member {Object} rightLiteral
 */
NetCordaCoreNodeServicesVaultColumnPredicateEqualityComparisonAllOf.prototype['rightLiteral'] = undefined;





/**
 * Allowed values for the <code>operator</code> property.
 * @enum {String}
 * @readonly
 */
NetCordaCoreNodeServicesVaultColumnPredicateEqualityComparisonAllOf['OperatorEnum'] = {

    /**
     * value: "EQUAL"
     * @const
     */
    "EQUAL": "EQUAL",

    /**
     * value: "NOT_EQUAL"
     * @const
     */
    "NOT_EQUAL": "NOT_EQUAL",

    /**
     * value: "EQUAL_IGNORE_CASE"
     * @const
     */
    "EQUAL_IGNORE_CASE": "EQUAL_IGNORE_CASE",

    /**
     * value: "NOT_EQUAL_IGNORE_CASE"
     * @const
     */
    "NOT_EQUAL_IGNORE_CASE": "NOT_EQUAL_IGNORE_CASE"
};



export default NetCordaCoreNodeServicesVaultColumnPredicateEqualityComparisonAllOf;

