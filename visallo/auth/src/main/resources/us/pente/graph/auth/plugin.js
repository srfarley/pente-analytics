define(
    ['configuration/plugins/registry'],
    function(registry) {
    'use strict';

    registry.registerExtension('org.visallo.authentication', {
        componentPath: 'us/pente/graph/auth/authentication'
    })
});
