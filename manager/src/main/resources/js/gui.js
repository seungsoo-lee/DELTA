var deltalog = "",
	channellog = "",
	applog = "",
	hostlog = "",
	newline="",
	isNewlineHandled = true;
function handleDeltaData(data){
	if ( isNewlineHandled == false ) return;
	var old = deltalog;
	if( old.length != data.length ){
		var line = String(data).replace(old,"");
		if( line.indexOf("*") != -1 ){
			newline = line;
			isNewlineHandled = false;
		}
		deltalog = data;
	}
}
function handleChannelData(data){
	if ( isNewlineHandled == false ) return;
	var old = channellog; 
	if( old.length != data.length ){
		var line = String(data).replace(old,"");
		if( line.indexOf("*") != -1 ){
			newline = line;
			isNewlineHandled = false;
		}
		channellog = data;
	}
}
function handleAppData(data){
	if ( isNewlineHandled == false ) return;
	var old = applog;
	if( old.length != data.length ){
		var line = String(data).replace(old,"");
		if( line.indexOf("*") != -1 ){
			newline = line;
			isNewlineHandled = false;
		}
		applog = data;
	}
}
function handleHostData(data){
	if ( isNewlineHandled == false ) return;
	var old = hostlog;
	if( old.length != data.length ){
		var line = String(data).replace(old,"");
		if( line.indexOf("*") != -1 ){
			newline = line;
			isNewlineHandled = false;
		}
		hostlog = data;
	}
}

window.onload = function () {
	var lineSep = "\n";
	var s = Snap(1400,1050);
	Snap.load("DELTA.svg", function (f) {
		var manager = f.select("#Manager"),
			DP =  f.select("#DP").attr({opacity: 0.3}),
			HostAgent = f.select("#HostAgent_1_").attr({opacity: 0.3}),
			ChannelAgent = f.select("#ChannelAgent").attr({opacity: 0.3}),
			AppAgent = f.select("#AppAgent").attr({opacity: 0.3}),
			CP = f.select("#CP").attr({opacity: 0.3}),
			AppCase1 = f.select("#AppCase1").attr({opacity: 0}),
			AppCase2 = f.select("#AppCase2").attr({opacity: 0}),
			HostC = f.select("#HostC_2_").attr({opacity: 0}),
			SubDB = f.select("#SubDB").attr({opacity: 0}),
			Before = f.select("#Before").attr({opacity: 0}),
			After = f.select("#After").attr({opacity: 0}),
			AccessDB = f.select("#AccessDB").attr({opacity: 0}),
			Dead_CP = f.select("#DeadCP").attr({opacity: 0}),
			Dead_OF1 = f.select("#DeadOF1").attr({opacity: 0}),
			Dead_OF2 = f.select("#DeadOF2").attr({opacity: 0}),
			FlowRuleDB = f.select("#FlowRuleDB").attr({opacity: 0}),
			FlowRuleOF1 = f.select("#FlowRuleOF1").attr({opacity: 0}),
			FlowRuleOF2 = f.select("#FlowRuleOF2").attr({opacity: 0}),
			Flow_mod = {
				obj: f.select("#FlowMod").attr({opacity: 0}),
				X: f.select("#FlowModRect").attr("x"),
				Y: f.select("#FlowModRect").attr("y")
			},
			PKT_IN = {
				obj: f.select("#PKTIN").attr({opacity: 0}),
				X: f.select("#PKTINRect").attr("x"),
				Y:  f.select("#PKTINRect").attr("y")
			},
			Rule = {
				obj: f.select("#Rule").attr({opacity: 0}),
				X: f.select("#RuleRect").attr("x"),
				Y: f.select("#RuleRect").attr("y")
			},
			PKT = {
				obj: f.select("#PKT_5_").attr({opacity: 0}),
				X: f.select("#PKTRect").attr("x"),
				Y: f.select("#PKTRect").attr("y")
			},
			CMD = {
				obj: f.select("#CMD").attr({opacity: 0}),
				X: f.select("#CMDRect").attr("x"),
				Y: f.select("#CMDRect").attr("y")
			},
			X = {
				obj: f.select("#Null").attr({opacity: 0}),
				X: f.select("#NullRect").attr("x"),
				Y: f.select("#NullRect").attr("y")
			},
			CMD_Manager = f.select("#CMD-Manager"),
			CMD_DP = f.select("#CMD-DP"),
			CMD_CH = f.select("#CMD-CH"),
			CMD_CP = f.select("#CMD-CP"),
			OF_OF1= f.select("#OF-OF1"),
			OF_OF2= f.select("#OF-OF2"),
			OF_Hub= f.select("#OF-Hub"),
			OF_Agent= f.select("#OF-Agent"),
			OF_App= f.select("#OF-App"),
			OF_Core= f.select("#OF-Core"),
			PKT_Hub= f.select("#PKT-Hub"),
			PKT_OF1= f.select("#PKT-OF1"),
			PKT_OF2= f.select("#PKT-OF2"),
			PKT_HB= f.select("#PKT-HB"),
			PKT_HA= f.select("#PKT-HA"),
			PKT_Agent= f.select("#PKT-Agent"),
			PKT_App= f.select("#PKT-App"),
			PKT_Core= f.select("#PKT-Core")
			;
		s.add(f);
		setInterval(function() {
			if( isNewlineHandled == true ) return;
			newline = newline.slice(newline.indexOf("* "), newline.length);
			var line = newline.slice(0, newline.indexOf(lineSep));
			if( line.indexOf("* On/Off | ") != -1 )
				handleOnOff(line);
			else if( line.indexOf("* SendPKT | ") != -1 ){
				handleSend(line);
			}
			else if( line.indexOf("* RecvPKT | ") != -1 ){
				//handleRecv(line);
			}
			else if( line.indexOf("* Data | ") != -1 ){
				//handleData(line);
			}
			else if( line.indexOf("* State | ") != -1 ){
				//handleState(line);
			}
			var nextIndex = newline.slice(1, newline.length).indexOf("* ");
			if (nextIndex == -1) isNewlineHandled = true;
			else newline = newline.slice(nextIndex, newline.length);
		}, 100);
		function show(obj, interval, callback) {
			if (typeof callback === "undefined")
				obj.animate({opacity: 1}, interval);
			else obj.animate({opacity: 1}, interval, callback);
		}
		function hide(obj, interval, opa) {
			obj.animate({opacity: opa}, interval);
		}
		function blink(obj, interval, callback) {
			if (typeof callback === "undefined"){
				obj.animate({opacity: 0}, interval/2, function(){
					obj.animate({opacity: 1}, interval/2);
				});
			}else{
				obj.animate({opacity: 0}, interval/2, function(){
					obj.animate({opacity: 1}, interval/2, callback);
				});
			}
		}
		function handleOnOff(line){
			var type = "* On/Off | ";
			var sep = " : ";
			var length = type.length;
			var component = line.slice(line.indexOf(type) + type.length, line.indexOf(sep));
			var state = line.slice(line.indexOf(sep) + sep.length, line.length);
			var obj;
			if( component == "App agent" ) obj = AppAgent;
			else if( component == "Host agent") obj = HostAgent;
			else if( component == "Channel agent") obj = ChannelAgent;
			else if( component == "OpenDaylight") obj = CP;
			else if( component == "Floodlight") obj = CP;
			else if( component == "ONOS") obj = CP;
			else if( component == "Ryu") obj = CP;
			else if( component == "DP") 	obj = DP;
			if( state == "On" ) show(obj, 1000);
			else if( state == "Off" ){
				hide(obj, 1000, 0.3);
				if( obj == CP ) hide(AppAgent, 1000, 0.3);
			}
		}
		function handleSend(line){
			var type = "* SendPKT | ";
			var sep1 = " : ";
			var sep2 = " --> ";
			var sep3 = " = ";
			var command = line.slice(line.indexOf(type) + type.length, line.indexOf(sep1));
			var src = line.slice(line.indexOf(sep1) + sep1.length, line.indexOf(sep2));
			var dst = line.slice(line.indexOf(sep2) + sep2.length, line.indexOf(sep3));
			var content = line.slice(line.indexOf(sep3) + sep3.length, line.length);
			var cmd_obj, src_obj, dst_obj;
			if( command == "CMD" ) cmd_obj = CMD;
			//else if( contents == " TODO: implement other packet send
			if( src == "Manager" ) src_obj = CMD_Manager;
			//else if (src == "... TODO
			if( dst == "Host agent" ) dst_obj = CMD_DP;
			else if( dst == "Channel agent" ) dst_obj = CMD_CH;
			else if( dst == "App agent" ) dst_obj = CMD_CP;
			sendPkt(cmd_obj, src_obj, dst_obj);
		}
		function setPos(target, dst){
			target.obj.attr({
				transform: "translate("+(dst.attr("x")-target.X) + "," + (dst.attr("y")-target.Y) + ")"
			});
		}
		function sendPkt(target, src, dst){
			setPos(target, src);
			target.obj.animate({opacity: 1}, 1000, function(){
				target.obj.animate({
					transform: "translate("+(dst.attr("x")-target.X) + "," + (dst.attr("y")-target.Y) + ")"
				}, 2000, function(){
					target.obj.animate({opacity: 0}, 1000);
				});
			});
		}
	});
};
