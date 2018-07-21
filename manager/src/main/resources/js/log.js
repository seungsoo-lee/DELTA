/**
 * Created by jinwookim on 2018. 7. 21..
 */

$(document).ready(function () {
    var testcase_table = $('#testcase-table').DataTable({
        dom: 'frBtlip',
        buttons: [
            'selectAll',
            'selectNone',
            {
                text: "Queue selected",
                action: function (e, dt, node, config) {

                    var queueArray = new Array();
                    dt.rows({selected: true}).every(function (rowIdx, tableLoop, rowLoop) {
                        queueArray.push(this.data().casenum);
                    });

                    $.ajax({
                        url: "/json/testqueue/post",
                        type: "POST",
                        data: queueArray.toString(),
                        dataType: "text",
                        contentType: "text/plain",
                        async: false,
                        success: function (data) {
                            alert(data);
                        },
                        error: function (xhr, ajaxOptions, thrownError) {
                            alert(xhr.status);
                            alert(thrownError);
                        }

                    });
                }
            }
        ],
        select: {
            style: 'multi'
        },
        lengthMenu: [10, 20, 50, 100],
        searching: true,
        'columns': [
            {'data': 'category'},
            {'data': 'casenum'},
            {'data': 'name'},
        ],
        'ajax': {
            'url': '/json/testcases',
            'dataSrc': ""
        },
        "createdRow": function (row, data) {
            $(row).attr('title', data['desc']);
            $(row).tooltip({
                "delay": 0,
                "track": true,
                "fade": 250
            });
        },
    });

    var queue_table = $('#queue-table').DataTable({
        dom: 'frtlip',
        select: {
            style: 'multi'
        },
        lengthMenu: [10, 20, 50, 100],
        searching: true,
        'columns': [
            {'data': 'index'},
            {'data': 'time'},
            {'data': 'category'},
            {'data': 'casenum'},
            {'data': 'name'},
            {'data': 'status'},
            {'data': 'result'}
        ],
        'ajax': {
            'url': '/json/testqueue/get',
            'dataSrc': ""
        },
        "fnRowCallback": function (nRow, aData) {
            if (aData['status'] == "RUNNING") {
                $('td', nRow).css('background-color', '#ffff99');
            }
            else if (aData['status'] == "COMPLETE") {
                if (aData['result'] == "FAIL") {
                    $('td', nRow).css('background-color', '#ff9999');
                }
                else if (aData['result'] == "PASS") {
                    $('td', nRow).css('background-color', '#ccff99');
                }
                else {
                    $('td', nRow).css('background-color', '#ffffff');
                }
            }
        },
        "createdRow": function (row, data) {
            $(row).attr('title', data['desc']);
            $(row).tooltip({
                "delay": 0,
                "track": true,
                "fade": 250
            });
        },
    });

    setInterval(function () {
        queue_table.ajax.reload();
    }, 3000);

    var managerTimer = null;
    var startManagerLog = function () {
        managerTimer = setInterval(function () {

            $.ajax({
                url: '/text/getlog',
                dataType: 'text',
                contentType: "text/plain",
                async: false,
                success: function (data) {
                    var old = $('#deltalog').val();
                    if (old.length != data.length) {
                        $('#deltalog').val(data);
                        $('#deltalog').scrollTop($('#deltalog')[0].scrollHeight);
                    }
                }
            });
        }, 1000);
        return managerTimer;
    }

    $('#deltalog').on({
        mouseenter: function() {
            clearInterval(managerTimer);
        },
        mouseleave: function() {
            managerTimer = startManagerLog();
        }
    }).trigger('mouseenter');


    var channelTimer = null;
    var startChannelLog = function () {
        channelTimer = setInterval(function () {

            $.ajax({
                url: '/text/getchannel',
                dataType: 'text',
                contentType: "text/plain",
                async: false,
                success: function (data) {
                    var old = $('#channellog').val();
                    if (old.length != data.length) {
                        $('#channellog').val(data);
                        $('#channellog').scrollTop($('#channellog')[0].scrollHeight);
                    }
                }
            });
        }, 1000);
    }

//        $('#channellog')
//            .hover(function () {
//                clearInterval(channelTimer)
//            }, function () {
//                startChannelLog();
//            });

    var appTimer = null;
    var startApplog = function () {
        appTimer = setInterval(function () {

            $.ajax({
                url: '/text/getapp',
                dataType: 'text',
                contentType: "text/plain",
                async: false,
                success: function (data) {
                    var old = $('#applog').val();
                    if (old.length != data.length) {
                        $('#applog').val(data);
                        $('#applog').scrollTop($('#applog')[0].scrollHeight);
                    }
                    $('#applog').hover(function () {
                        clearInterval(appTimer)
                    }, function () {
                        appTimer = startApplog();
                    });

                }
            });
        }, 1000);
        return appTimer;
    }

    $('#applog').on({
        mouseenter: function() {
            clearInterval(appTimer);
        },
        mouseleave: function() {
            appTimer = startApplog();
        }
    }).trigger('mouseenter');

    var hostTimer = null;
    var startHostlog = function () {
        hostTimer = setInterval(function () {

            $.ajax({
                url: '/text/gethost',
                dataType: 'text',
                contentType: "text/plain",
                async: false,
                success: function (data) {
                    var old = $('#hostlog').val();
                    if (old.length != data.length) {
                        $('#hostlog').val(data);
                        $('#hostlog').scrollTop($('#hostlog')[0].scrollHeight);
                    }
                }
            });
        }, 1000);
    }

//        $('#hostlog').hover(function () {
//            clearInterval(hostTimer)
//        }, function () {
//            startHostlog();
//        });

    startManagerLog();
    startApplog();
    startChannelLog();
    startHostlog();

});