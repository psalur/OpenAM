/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2012 ForgeRock AS. All rights reserved.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * $Id$
 */
package org.forgerock.restlet.ext.oauth2.flow;

import org.forgerock.restlet.ext.oauth2.OAuth2;
import org.forgerock.restlet.ext.oauth2.OAuth2Utils;
import org.forgerock.restlet.ext.oauth2.OAuthProblemException;
import org.forgerock.restlet.ext.oauth2.model.AccessToken;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.routing.Redirector;

import java.util.Map;
import java.util.Set;

/**
 * @author $author$
 * @version $Revision$ $Date$
 */
public class ImplicitGrantServerResource extends AbstractFlow {

    protected boolean decision_allow = false;

    /**
     * Developers should note that some user-agents do not support the inclusion of a fragment component in the HTTP
     * "Location" response header field.  Such clients will require using other methods for redirecting the client
     * than a 3xx redirection response.  For example, returning an HTML page which includes a 'continue'
     * button with an action linked to the redirection URI.
     * <p/>
     * If TLS is not available, the authorization server SHOULD warn the resource owner about the insecure endpoint
     * prior to redirection.
     *
     * @return
     */
    @Get("html")
    public Representation represent() {
        resourceOwner = getAuthenticatedResourceOwner();

        String approval_prompt = OAuth2Utils.getRequestParameter(getRequest(), OAuth2.Custom.APPROVAL_PROMPT, String.class);

        if (true) {
            /*
            APPROVAL_PROMPT = false AND CLIENT.AUTO_GRANT
             */

            //Validate the client
            client = validateRemoteClient();
            //Validate Redirect URI throw exception
            sessionClient = client.getClientInstance(OAuth2Utils.getRequestParameter(getRequest(),
                    OAuth2.Params.REDIRECT_URI, String.class));

            //The target contains the state
            String state = OAuth2Utils.getRequestParameter(getRequest(), OAuth2.Params.STATE, String.class);

            //Get the requested scope
            String scope_before = OAuth2Utils.getRequestParameter(getRequest(), OAuth2.Params.SCOPE, String.class);
            //Validate the granted scope
            Set<String> checkedScope = getCheckedScope(scope_before, client.getClient().allowedGrantScopes(),
                    client.getClient().defaultGrantScopes());

            AccessToken token = createAccessToken(checkedScope);
            Form tokenForm = tokenToForm(token.convertToMap());

            /*
              scope
                OPTIONAL, if identical to the scope requested by the client,
                otherwise REQUIRED.  The scope of the access token as described
                by Section 3.3.
             */
            if (isScopeChanged()) {
                tokenForm.add(OAuth2.Params.SCOPE, OAuth2Utils.join(checkedScope, OAuth2Utils.getScopeDelimiter(getContext())));
            }
            if (null != state) {
                tokenForm.add(OAuth2.Params.STATE, state);
            }

            Reference redirectReference = new Reference(sessionClient.getRedirectUri());
            redirectReference.setFragment(tokenForm.getQueryString());

            Redirector dispatcher = new Redirector(getContext(), redirectReference.toString(), Redirector.MODE_CLIENT_FOUND);
            dispatcher.handle(getRequest(), getResponse());
            return getResponseEntity();

        } else {
            //Build approval page data
            return getPage("authorize.ftl", getDataModel());
        }
    }

    @Override
    protected void validateMethod() throws OAuthProblemException {
        if (!Method.GET.equals(getMethod())) {
            throw OAuthProblemException.handleOAuthProblemException("Implicit grand type supports only GET method");
        }
    }

    @Override
    protected String[] getRequiredParameters() {
        return new String[]{OAuth2.Params.RESPONSE_TYPE, OAuth2.Params.CLIENT_ID};
    }

    /**
     * This method is intended to be overridden by subclasses.
     *
     * @param checkedScope
     * @return
     * @throws org.forgerock.restlet.ext.oauth2.OAuthProblemException
     *
     */
    protected AccessToken createAccessToken(Set<String> checkedScope) {
        return getTokenStore().createAccessToken(client.getClient().getAccessTokenType(), checkedScope,
                OAuth2Utils.getContextRealm(getContext()), resourceOwner.getIdentifier(), sessionClient);
    }

    protected Form tokenToForm(Map<String, Object> token) {
        Form result = new Form();
        for (Map.Entry<String, Object> entry : token.entrySet()) {
            result.add(entry.getKey(), entry.getValue().toString());
        }
        return result;
    }

}
