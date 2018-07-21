var ws = null;
ws = new WebSocket("ws://127.0.0.1:7070")
var result = ws.send("/new/echo");

var app = angular.module('myApp', []);
app.controller('myCtrl', function ($scope) {
    $scope.echo = result;
});