/**
 * Created by jinwookim on 2018. 7. 17..
 */
/* global delta */
/* global util */
/******************************************************************/
var delta = {};
var NUM_SERVERS = 4;
var ARC_WIDTH = 5;
delta.server = function (id, name, peers) {
    return {
        id: id,
        name: name,
        peers: peers,
    };
};

var makeState = function (initial) {
    var self = {
        current: initial,
    }
    return self;
};

// util.circleCoord = function(frac, cx, cy, r) {
//     var radians = 2 * Math.PI * (0.75 + frac);
//     return {
//         x: cx + r * Math.cos(radians),
//         y: cy + r * Math.sin(radians),
//     };
// };

var rectCoord = function (frac, x, y, width, height) {
    return {
        x: x + width,
        y: y + height,
    };
};

/******************************************************************/

$(function () {
    state = makeState({
        servers: [],
        messages: [],
    });

    var SVG = function (tag) {
        return $(document.createElementNS('http://www.w3.org/2000/svg', tag));
    };

    var peers = ['manager', 'controller', 'channel', 'host'];
    state.current.servers.push(delta.server(1, 'manager', peers));
    state.current.servers.push(delta.server(2, 'controller', peers));
    state.current.servers.push(delta.server(3, 'channel', peers));
    state.current.servers.push(delta.server(4, 'host', peers));

    var ringSpec = {
        cx: 600,
        cy: 200,
        r: 100,
    };

    var rectSpec = {
        x: 100,
        y: 100,
        width: 200,
        height: 200,
    };

    var serverSpec = function (id) {
        var coord = util.circleCoord((id - 1) / NUM_SERVERS,
            ringSpec.cx, ringSpec.cy, ringSpec.r);
        return {
            cx: coord.x,
            cy: coord.y,
            r: 30,
        };
    };

    var serverSpec2 = function (id) {
        var coord = rectCoord((id - 1) / NUM_SERVERS,
            rectSpec.x, rectSpec.y, rectSpec.width, rectSpec.height);
        return {
            x: coord.x,
            y: coord.y,
            width: 30,
            height: 30,
        };
    };

    var svg = $('svg');
    $('#ring', svg).attr(ringSpec);
    $('#rect', svg).attr(rectSpec);

    // draw delta servers

    state.current.servers.forEach(function (server) {
        var s = serverSpec(server.id);
        $('#delta', svg).append(
            SVG('g')
                .attr('id', 'server-' + server.id)
                .attr('class', 'server')
                .append(SVG('text')
                    .attr('class', 'serverid')
                    .text(server.name)
                    .attr(util.circleCoord((server.id - 1) / NUM_SERVERS,
                        ringSpec.cx, ringSpec.cy + 50, ringSpec.r)))
                .append(SVG('a')
                    .append(SVG('circle')
                        .attr('class', 'background')
                        .attr(s))
                    .append(SVG('g')
                        .attr('class', 'votes'))
                    .append(SVG('path')
                        .attr('style', 'stroke-width: ' + ARC_WIDTH))
                    .append(SVG('text')
                        .attr('class', 'term')
                        .attr({x: s.cx, y: s.cy}))
                ));
    });

    var makeRect = function (server, a, b) {
        $('#delta', svg).append(
            SVG('g')
                .attr('id', 'server-' + server.id)
                .attr('class', 'server')
                .apeend(SVG('text')
                    .attr('class', 'serverid')
                    .text(server.name)
                    .attr(rectSpec.x, rectSpec.y, a * rectSpec.width, b * rectSpec.height)
                )
                .append(SVG('circle')
                    .attr('class', 'background')
                    .attr(s))
        );
    }
    // draw delta servers in rectangle
    state.current.servers.forEach(function (server) {
        makeRect(server, 1, 1);
    });


});

