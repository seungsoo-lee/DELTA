var deltalog = "",
	channellog = "",
	applog = "",
	hostlog = "",
	newline="",
	isNewlineHandled = true,
	firstLine = true;
function handleDeltaData(data){
	if ( isNewlineHandled == false ) return;
	var old = deltalog;
	if( old.length != data.length ){
		var line = String(data).replace(old,"");
		if( line.indexOf("*") != -1 ){
			if( deltalog.length != 0 ){
				newline = line;
				isNewlineHandled = false;
			}
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
			if( channellog.length != 0 ){
				newline = line;
				isNewlineHandled = false;
			}
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
			if( applog.length != 0 ){
				newline = line;
				isNewlineHandled = false;
			}
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
			if( hostlog.length != 0 ){
				newline = line;
				isNewlineHandled = false;
			}
		}
		hostlog = data;
	}
}

window.onload = function () {
	var lineSep = "\n";
	var didToss = false;
	var data_obj;
	var handleMsg = false;
	var s = Snap(1400,1050);
	Snap.load("DELTA.svg", function (f) {
		var manager = f.select("#Manager"),
			DP =  f.select("#DP").attr({opacity: 1}),
			Connection = f.select("#Connection").attr({opacity: 0.3}),
			HostAgent = f.select("#HostAgent").attr({opacity: 0.3}),
			ChannelAgent = f.select("#ChannelAgent").attr({opacity: 0.3}),
			AppAgent = f.select("#AppAgent").attr({opacity: 0.3}),
			CP = f.select("#CP").attr({opacity: 0.3}),
			//HostC = f.select("#HostC").attr({opacity: 0}),
			SubDB = f.select("#SubDB").attr({opacity: 0}),
			PacketInList = {
				obj: f.select("#PKTINList").attr({opacity: 0}),
				text1: f.select("#PKTINList1Text"),
				box1: f.select("#PKTINList1Box"),
				text2: f.select("#PKTINList2Text"),
				box2: f.select("#PKTINList2Box"),
				text3: f.select("#PKTINList3Text"),
				box3: f.select("#PKTINList3Box"),
				text4: f.select("#PKTINList4Text"),
				box4: f.select("#PKTINList4Box"),
				text5: f.select("#PKTINList5Text"),
				box5: f.select("#PKTINList5Box"),
				text6: f.select("#PKTINList6Text"),
				box6: f.select("#PKTINList6Box"),
				text7: f.select("#PKTINList7Text"),
				box7: f.select("#PKTINList7Box")
			},
			AccessDB = f.select("#AccessDB").attr({opacity: 0}),
			Dead_CP = f.select("#DeadCP").attr({opacity: 0}),
			Dead_OF1 = f.select("#DeadOF1").attr({opacity: 0}),
			Dead_OF2 = f.select("#DeadOF2").attr({opacity: 0}),
			FDB = {
				obj : f.select("#FDB").attr({opacity: 0}),
				box0 : f.select("#FDBBox0"),
				box1 : f.select("#FDBBox1"),
				box2 : f.select("#FDBBox2"),
				box3 : f.select("#FDBBox3"),
				box4 : f.select("#FDBBox4"),
				box5 : f.select("#FDBBox5"),
				text11 : f.select("#FDB11"),
				text12 : f.select("#FDB12"),
				text13 : f.select("#FDB13"),
				text14 : f.select("#FDB14"),
				text15 : f.select("#FDB15"),
				text21 : f.select("#FDB21"),
				text22 : f.select("#FDB22"),
				text23 : f.select("#FDB23"),
				text24 : f.select("#FDB24"),
				text25 : f.select("#FDB25"),
				text31 : f.select("#FDB31"),
				text32 : f.select("#FDB32"),
				text33 : f.select("#FDB33"),
				text34 : f.select("#FDB34"),
				text35 : f.select("#FDB35"),
				text41 : f.select("#FDB41"),
				text42 : f.select("#FDB42"),
				text43 : f.select("#FDB43"),
				text44 : f.select("#FDB44"),
				text45 : f.select("#FDB45"),
				text51 : f.select("#FDB51"),
				text52 : f.select("#FDB52"),
				text53 : f.select("#FDB53"),
				text54 : f.select("#FDB54"),
				text55 : f.select("#FDB55")
			}
			FOF1 = {
				obj : f.select("#FOF1").attr({opacity: 0}),
				box0 : f.select("#FOFBox10"),
				box1 : f.select("#FOFBox11"),
				box2 : f.select("#FOFBox12"),
				box3 : f.select("#FOFBox13"),
				text11 : f.select("#i111"),
				text12 : f.select("#i112"),
				text13 : f.select("#i113"),
				text14 : f.select("#i114"),
				text21 : f.select("#i121"),
				text22 : f.select("#i122"),
				text23 : f.select("#i123"),
				text24 : f.select("#i124"),
				text31 : f.select("#i131"),
				text32 : f.select("#i132"),
				text33 : f.select("#i133"),
				text34 : f.select("#i134")
			}
			FOF2 = {
				obj : f.select("#FOF2").attr({opacity: 0}),
				box0 : f.select("#FOFBox20"),
				box1 : f.select("#FOFBox21"),
				box2 : f.select("#FOFBox22"),
				box3 : f.select("#FOFBox23"),
				text11 : f.select("#i211"),
				text12 : f.select("#i212"),
				text13 : f.select("#i213"),
				text14 : f.select("#i214"),
				text21 : f.select("#i221"),
				text22 : f.select("#i222"),
				text23 : f.select("#i223"),
				text24 : f.select("#i224"),
				text31 : f.select("#i231"),
				text32 : f.select("#i232"),
				text33 : f.select("#i233"),
				text34 : f.select("#i234")
			}
			Flow_mod = {
				obj: f.select("#FlowMod").attr({opacity: 0}),
				textBox: f.select("#RuleTextBox").attr({opacity: 0}),
				text: f.select("#RuleText"),
				inRect: f.select("#RuleRect"), 
				outRect: f.select("#FlowModBody"), 
				X: f.select("#FlowModRect").attr("x"),
				Y: f.select("#FlowModRect").attr("y"),
				Running: false
			},
			PKT_IN = {
				obj: f.select("#PKTIN").attr({opacity: 0}),
				textBox: f.select("#PKTTextBox").attr({opacity: 0}),
				text: f.select("#PKTText"),
				inRect: f.select("#PKTRect"), 
				outRect: f.select("#PKTINBody"), 
				X: f.select("#PKTINRect").attr("x"),
				Y: f.select("#PKTINRect").attr("y"),
				Running: false
			},
			CMD = {
				obj: f.select("#CMD").attr({opacity: 0}),
				textBox: f.select("#CMDTextBox").attr({opacity: 0}),
				text: f.select("#CMDText"),
				X: f.select("#CMDRect").attr("x"),
				Y: f.select("#CMDRect").attr("y"),
				Running: false
			},
			OF = {
				obj: f.select("#OF").attr({opacity: 0}),
				textBox: f.select("#OFTextBox").attr({opacity: 0}),
				text: f.select("#OFText"),
				X: f.select("#OFRect").attr("x"),
				Y: f.select("#OFRect").attr("y"),
				Running: false
			},
			CMD_Manager = f.select("#CMD-Manager"),
			CMD_DP = f.select("#CMD-DP"),
			CMD_CH = f.select("#CMD-CH"),
			CMD_CP = f.select("#CMD-CP"),
			OF_Hub= f.select("#OF-Hub"),
			OF_OF1= f.select("#OF-OF1"),
			OF_OF2= f.select("#OF-OF2"),
			OF_Agent= f.select("#OF-Agent"),
			OF_App= f.select("#OF-App"),
			OF_Core= f.select("#OF-Core"),
			OF_HB= f.select("#OF-HB"),
			OF_HA= f.select("#OF-HA"),
			Explain1 = f.select("#Explain1").attr({
				opacity: 0,
				"text-anchor": "middle"
			}),
			Explain2 = f.select("#Explain2").attr({
				opacity: 0,
				"text-anchor": "middle"
			});
			Explain3 = f.select("#Explain3").attr({
				opacity: 0,
				"text-anchor": "middle"
			});
		CMD.obj.hover( function(){
			CMD.textBox.attr({opacity: 1});
		}, function(){
			CMD.textBox.attr({opacity: 0});
		});
		PKT_IN.inRect.hover( function(){
			PKT_IN.textBox.attr({opacity: 1});
		}, function(){
			PKT_IN.textBox.attr({opacity: 0});
		});
		Flow_mod.inRect.hover( function(){
			Flow_mod.textBox.attr({opacity: 1});
		}, function(){
			Flow_mod.textBox.attr({opacity: 0});
		});
		OF.obj.hover( function(){
			OF.textBox.attr({opacity: 1});
		}, function(){
			OF.textBox.attr({opacity: 0});
		});
		FDB.obj.attr({ transform: "translate(0, -70)" });
		s.add(f);
		setInterval(function() {
			if( isNewlineHandled == true ) return;
			newline = newline.slice(newline.indexOf("* "), newline.length);
			var line = newline.slice(0, newline.indexOf(lineSep));
			if( line.indexOf("* On/Off | ") != -1 )
				handleOnOff(line);
			else if( line.indexOf("* SendPKT | ") != -1 ){
				if( handlePKT(line, "* SendPKT | ") == false ) return;
			}
			else if( line.indexOf("* RecvPKT | ") != -1 ){
				if( handlePKT(line, "* RecvPKT | ") == false ) return;
			}
			else if( line.indexOf("* Data | ") != -1 ){
				handleData(line);
			}
			else if( line.indexOf("* Action | ") != -1 ){
				handleAction(line);
			}
			else if( line.indexOf("* Test | ") != -1 ){
				handleTest(line);
			}
			else if( line.indexOf("* Result | ") != -1 ){
				handleResult(line);
			}
			var nextIndex = newline.slice(1, newline.length).indexOf("* ");
			if (nextIndex == -1)
				isNewlineHandled = true;
			else newline = newline.slice(nextIndex, newline.length);
		}, 100);
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
			else if( component == "Connection")	obj = Connection;
			if( state == "On" ){
				show(obj, 1000);
				CMD.Running = false;
				PKT_IN.Running = false;
				Flow_mod.Running = false;
				OF.Running = false;
			}
			else if( state == "Off" ){
				OF.Running = true;
				CMD.Running = true;
				PKT_IN.Running = true;
				Flow_mod.Running = true;
				hide(obj, 1000, 0.3);
				if( obj == CP ){
					hide(AppAgent, 1000, 0.3);
					hide(Connection, 1000, 0.3);
				}
				hide(CMD.obj, 300, 0);
				hide(PKT_IN.obj, 300, 0);
				hide(Flow_mod.obj, 300, 0);
				hide(OF.obj, 300, 0);
				hide(FDB.obj, 300, 0);
				hide(FOF1.obj, 300, 0);
				hide(FOF2.obj, 300, 0);
				hide(AccessDB, 300, 0);
				hide(PacketInList.obj, 300, 0);
				hide(Dead_CP, 300, 0);
				hide(Explain1, 300, 0);
				hide(Explain2, 300, 0);
				hide(Explain3, 300, 0);
				initTable(FDB);
				initTable(FOF1);
				initTable(FOF2);
				initTable(PacketInList);
				FDBIndex = 1;
				FOFIndex1 = 1;
				FOFIndex2 = 1;
				handleMsg = false;
			}
			if( (obj == CP) && (state == "Off") ) didToss = false;
		}
		function handlePKT(line, type){
			var sep1 = " : ";
			var sep2 = " --> ";
			var sep3 = " = ";
			var command = line.slice(line.indexOf(type) + type.length, line.indexOf(sep1));
			var src = line.slice(line.indexOf(sep1) + sep1.length, line.indexOf(sep2));
			var dst = line.slice(line.indexOf(sep2) + sep2.length, line.indexOf(sep3));
			var content = line.slice(line.indexOf(sep3) + sep3.length, line.length);
			var cmd_obj, src_obj, dst_obj;
			switch (command){
				case "CMD" :
					cmd_obj = CMD;
					break;
				case "PKT" :
				case "PKT_IN" :
					cmd_obj = PKT_IN;
					break;
				case "Rule" :
				case "Flow_mod" :
					cmd_obj = Flow_mod;
					break;
				case "OF" :
					cmd_obj = OF;
					break;
			}
			if( handleMsg ){
				if( cmd_obj != CMD ){
					if( (src != "App agent") || (dst != "Apps") ) 
						return;
				}
			}
			switch (command){
				case "PKT" :
				case "Rule":
					cmd_obj.outRect.attr({opacity: 0});
					cmd_obj.inRect.attr({opacity: 1});
					break;
				case "PKT_IN":
				case "Flow_mod":
					show(cmd_obj.outRect, 500);
					break;
			}
			switch (src){
				case "Manager" :
					src_obj = CMD_Manager;
					break;
				case "Host agent" :
					src_obj = OF_HA;
					break;
				case "HostB" :
					src_obj = OF_HB;
					break;
				case "Hub":
					src_obj = OF_Hub;
					break;
				case "OF1":
					src_obj = OF_OF1;
					break;
				case "OF2":
					src_obj = OF_OF2;
					break;
				case "Core":
					src_obj = OF_Core;
					break;
				case "App agent":
					src_obj = OF_Agent;
					break;
			}
			switch (dst){
				case "Host agent" :
					dst_obj = CMD_DP;
					break;
				case "Channel agent" :
					dst_obj = CMD_CH;
					break;
				case "App agent":
					if( command == "CMD" ) dst_obj = CMD_CP;
					else{
						dst_obj = OF_Agent;
						handleMsg = true;
					}
					break;
				case "Hub":
					dst_obj = OF_Hub;
					break;
				case "OF1":
					dst_obj = OF_OF1;
					break;
				case "OF2":
					dst_obj = OF_OF2;
					break;
				case "Core":
					dst_obj = OF_Core;
					break;
				case "Apps":
					dst_obj = OF_App;
					break;
			}

			if( didToss && (cmd_obj == PKT_IN)){
				if (dst == "Apps" ) return;
				else if( (dst == "App agent")  && (src == "OF1")) return;
			}
			if( command == "OF" ){
				content = content.slice(0, content.indexOf("("));
			}
			else if( command == "CMD" ){
				if( content.indexOf(",") != -1 ){
					if( content.length > 10 ) content = content.slice(0, content.indexOf(","));
				}
			}
			var font = 24;
			if( content.length > 14 ) font = Math.floor( (24 * 14) / content.length );
			cmd_obj.text.attr({
				"#text": content,
				"font-size": font
			});
			sendPkt(cmd_obj, src_obj, dst_obj);
		}
		var FDBIndex = 1;
		var FOFIndex1 = 1;
		var FOFIndex2 = 1;
		function handleData(line){
			var type;
			if( line.indexOf("* Data | [App-Agent] ") != -1 )
				type = "* Data | [App-Agent] ";
			else type = "* Data | ";
			line = line.slice( type.length, line.length);
			if( line.indexOf("List of Packet-In Listener") != -1 ){
				data_obj = PacketInList.obj;
				show(data_obj, 500);
				return;
			}
			else if( line.indexOf("<Controller>") != -1 ){
				show(FDB.obj, 500);
				show(FOF1.obj, 500);
				show(FOF2.obj, 500);
				data_obj = FDB;
				if( FDBIndex > 5 ) return;
				var ID = line.slice(line.indexOf("{id=") + 16, line.indexOf("{id=") + 18 );
				var IN, SRC, DST;
				if( line.indexOf("IN_PORT:") == -1 ){
					IN = "All";
					SRC = "All";
					DST = "All";
				}
				else{
					IN = line.slice(line.indexOf("IN_PORT:") + 8, line.indexOf("IN_PORT:") + 9 );
					SRC = line.slice(line.indexOf("ETH_SRC:") + 23, line.indexOf("ETH_SRC:") + 25 );
					DST = line.slice(line.indexOf("ETH_DST:") + 23, line.indexOf("ETH_DST") + 25 );
				}
				var Action;
				if( line.indexOf("OUTPUT:") == -1 ) Action = "Drop";
				else{
					line = line.slice(line.indexOf("OUTPUT:"), line.length);
					Action = "FWD " + line.slice(line.indexOf("OUTPUT:") + 7, line.indexOf("]"));
				}
				data_obj["text"+ FDBIndex + "1"].attr({"#text": ID});
				data_obj["text"+ FDBIndex + "2"].attr({"#text": IN});
				data_obj["text"+ FDBIndex + "3"].attr({"#text": SRC});
				data_obj["text"+ FDBIndex + "4"].attr({"#text": DST});
				data_obj["text"+ FDBIndex + "5"].attr({"#text": Action});
				FDBIndex++;
				return;
			}
			else if( line.indexOf("<Switch 01>") != -1 ){
				data_obj = FOF1;
				if( FOFIndex1 > 3 ) return;
				var IN, SRC, DST;
				if( line.indexOf("in_port=") == -1 ){
					IN = "All";
					SRC = "All";
					DST = "All";
				}
				else{
					IN = line.slice(line.indexOf("in_port=") + 8, line.indexOf("in_port=") + 9 );
					SRC = line.slice(line.indexOf("eth_src=") + 23, line.indexOf("eth_src=") + 25 );
					DST = line.slice(line.indexOf("eth_dst=") + 23, line.indexOf("eth_dst=") + 25 );
				}
				var Action;
				if( line.indexOf("(port=") == -1 ) Action = "Drop";
				else{
					line = line.slice(line.indexOf("(port="), line.length);
					Action = "FWD " + line.slice(line.indexOf("(port=") + 6, line.indexOf("," ));
				}
				var exist = false;
				for( var i = 1; i < FOFIndex1; i++ ){
					exist = true;
					if( data_obj["text"+ i + "1"].attr("#text") != IN )
						exist = false;
					if( data_obj["text"+ i + "2"].attr("#text") != SRC )
						exist = false;
					if( data_obj["text"+ i + "3"].attr("#text") != DST )
						exist = false;
					if( data_obj["text"+ i + "4"].attr("#text") != Action )
						exist = false;
					if( exist ) break;
				}
				if( !exist ){
					data_obj["text"+ FOFIndex1 + "1"].attr({"#text": IN});
					data_obj["text"+ FOFIndex1 + "2"].attr({"#text": SRC});
					data_obj["text"+ FOFIndex1 + "3"].attr({"#text": DST});
					data_obj["text"+ FOFIndex1 + "4"].attr({"#text": Action});
					FOFIndex1++;
				}
				return;
			}
			else if( line.indexOf("<Switch 02>") != -1 ){
				data_obj = FOF2;
				if( FOFIndex2 > 3 ) return;
				var IN, SRC, DST;
				if( line.indexOf("in_port=") == -1 ){
					IN = "All";
					SRC = "All";
					DST = "All";
				}
				else{
					IN = line.slice(line.indexOf("in_port=") + 8, line.indexOf("in_port=") + 9 );
					SRC = line.slice(line.indexOf("eth_src=") + 23, line.indexOf("eth_src=") + 25 );
					DST = line.slice(line.indexOf("eth_dst=") + 23, line.indexOf("eth_dst=") + 25 );
				}
				var Action;
				if( line.indexOf("(port=") == -1 ) Action = "Drop";
				else{
					line = line.slice(line.indexOf("(port="), line.length);
					Action = "FWD" + line.slice(line.indexOf("(port=") + 6, line.indexOf("," ));
				}
				var exist = false;
				for( var i = 1; i < FOFIndex2; i++ ){
					exist = true;
					if( data_obj["text"+ i + "1"].attr("#text") != IN )
						exist = false;
					if( data_obj["text"+ i + "2"].attr("#text") != SRC )
						exist = false;
					if( data_obj["text"+ i + "3"].attr("#text") != DST )
						exist = false;
					if( data_obj["text"+ i + "4"].attr("#text") != Action )
						exist = false;
					if( exist ) break;
				}
				if( !exist ){
					data_obj["text"+ FOFIndex2 + "1"].attr({"#text": IN});
					data_obj["text"+ FOFIndex2 + "2"].attr({"#text": SRC});
					data_obj["text"+ FOFIndex2 + "3"].attr({"#text": DST});
					data_obj["text"+ FOFIndex2 + "4"].attr({"#text": Action});
					FOFIndex2++;
				}
				return;
			}

			if( (data_obj == PacketInList.obj) ){
				var cnt = line.charAt( 0 );
				var app = line.slice( line.indexOf("[") + 1, line.indexOf("]"));
				var text, box;
				switch (cnt){
					case '1':
						text = PacketInList.text1;
						box = PacketInList.box1;
						break;
					case '2':
						text = PacketInList.text2;
						box = PacketInList.box2;
						break;
					case '3':
						text = PacketInList.text3;
						box = PacketInList.box3;
						break;
					case '4':
						text = PacketInList.text4;
						box = PacketInList.box4;
						break;
					case '5':
						text = PacketInList.text5;
						box = PacketInList.box5;
						break;
					case '6':
						text = PacketInList.text6;
						box = PacketInList.box6;
						break;
					case '7':
						text = PacketInList.text7;
						box = PacketInList.box7;
						break;
				}
				if( (text.attr("#text") != app) && ( text.attr("#text").length > 3 ) ){
					box.animate({"stroke": "#ff0000"}, 1000);
				}
				if (app == "AppAgent")
					box.animate({"fill": "#808080"},1000);
				else box.animate({"fill": "#ffffff"},1000);
				text.attr({"#text": app});
			}
		}
		function handleAction(line){
			if( line.indexOf("Randomize Packet-In subscription list") != -1 ) show(AccessDB, 500);
			if( line.indexOf("Remove content of Packet_In msg") != -1){
				if( !didToss )	hide(PKT_IN.inRect, 500);
			}
		}
		function handleTest(line){
			var type = "* Test | ";
			var word = line.slice(line.indexOf(type) + type.length, line.length);
			var line1, line2, line3;
			if( word.length > 120 ){
				var Index1 = Math.floor(word.length/3) +  word.slice( Math.floor(word.length / 3), word.length).indexOf(" ");
				var Index2 = Math.floor(word.length *2 / 3) + word.slice( Math.floor(word.length * 2 / 3), word.length).indexOf(" ");
				line1 = word.slice(0, Index1);
				line2 = word.slice(Index1 + 1, Index2);
				line3 = word.slice(Index2 + 1, word.length);
			}
			else if( word.length > 60 ){
				var spaceIndex = Math.floor(word.length/2) +  word.slice( Math.floor(word.length / 2), word.length).indexOf(" ");
				line1 = word.slice(0, spaceIndex);
				line2 = word.slice(spaceIndex+1, word.length);
				line3 = "";
			}
			else{
				line1 = word;
				line2 = "";
				line3 = "";
			}
			if( didToss && ( line.indexOf("The app agent toss")!= -1) ) return;
			if( didToss && ( line.indexOf("The app agent removes data field") != -1)) return;
			hide(Explain1, 1000, 0, function(){
				Explain1.attr({"#text": line1});
				show(Explain1, 800);
			});
			hide(Explain2, 1000, 0, function(){
				Explain2.attr({"#text": line2});
				show(Explain2, 800);
			});
			hide(Explain3, 1000, 0, function(){
				Explain3.attr({"#text": line3});
				show(Explain3, 800);
			});
			if( line.indexOf("The app agent toss") != -1 ) didToss=true;
		}
		function handleResult(line){
			var type = "* Result | ";
			if( line.indexOf("Ping response host unreachable") != -1 ) show(Dead_CP, 500);
			if( line.indexOf("No response") != -1 ) show(Dead_CP, 500);
			// TODO: insert log handler here
		}
		function setPos(target, dst){
			target.obj.attr({
				transform: "translate("+(dst.attr("x")-target.X) + "," + (dst.attr("y")-target.Y) + ")"
			});
		}
		function sendPkt(target, src, dst){
			if( target.Running ) return;
			target.Running = true;
			setPos(target, src);
			target.obj.animate({opacity: 1}, 500, function(){
				target.obj.animate({
					transform: "translate("+(dst.attr("x")-target.X) + "," + (dst.attr("y")-target.Y) + ")"
				}, 2000, function(){
					target.Running = false;
				});
			});
		}
		function show(obj, interval) {
			obj.animate({opacity: 1}, interval );
		}
		function hide(obj, interval, opa, callback) {
			if (typeof callback === "undefined")
				obj.animate({opacity: opa}, interval);
			else obj.animate({opacity: opa}, interval, callback);
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
		function initTable(obj){
			var rowMax, colMax;
			if( obj == PacketInList ){
				for( var i = 1; i < 8 ; i++ ) obj["text" + i ].attr({"#text": " "});
				return;
			}
			else{
				if( obj == FDB ){
					rowMax = 5;
					colMax = 5;
				}
				else if( (obj == FOF1) || (obj == FOF2) ){
					rowMax = 3;
					colMax = 4;
				}
				//obj.box0.attr({"#text": " "});
				for( var i = 1; i < rowMax + 1; i++ ){
					//obj["box" + i].attr({"#text": " "});
					for( var j = 1; j < colMax + 1; j++){
						obj["text" + i + j].attr({"#text": " "});
					}
				}
			}
		}
	});
};
