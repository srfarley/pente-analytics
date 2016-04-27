require([
    'configuration/plugins/registry',
    'util/vertex/formatters',
    'util/messages'
], function (
    registry,
    F,
    i18n
) {
    'use strict';

    registry.registerExtension('org.visallo.detail.toolbar', {
        title:  i18n('us.pente.graph.web.detail.toolbar.player.profile'),
        event: 'openPlayerProfile',
        canHandle: function(objects) {
            return objects.vertices.length === 1 && objects.edges.length === 0
                && objects.vertices[0].conceptType === 'http://pente.us/pente#player';
        }
    });

    $(document).on('openPlayerProfile', function(e, data) {
        var playerName = F.vertex.prop(data.vertices[0], 'http://pente.us/pente#playerName');
        var url = 'https://pente.org/gameServer/profile?viewName=' + playerName;
        window.open(url,'_blank');
    });
});
