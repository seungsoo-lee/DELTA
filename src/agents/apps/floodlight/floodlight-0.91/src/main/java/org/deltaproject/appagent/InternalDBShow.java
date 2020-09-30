package org.deltaproject.appagent;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.storage.IResultSet;
import net.floodlightcontroller.storage.IStorageSourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Access InternalDBShow
 */
public class InternalDBShow {
    protected static Logger logger = LoggerFactory.getLogger(InternalDBShow.class);

    protected IFloodlightProviderService floodlightProvider;

	protected IStorageSourceService storageSource;

	public static final String SWITCH_TABLE_NAME = "controller_switch";
	public static final String SWITCH_DATAPATH_ID = "dpid";
	public static final String SWITCH_SOCKET_ADDRESS = "socket_address";
	public static final String SWITCH_IP = "ip";
	public static final String SWITCH_CONTROLLER_ID = "controller_id";
	public static final String SWITCH_ACTIVE = "active";
	public static final String SWITCH_CONNECTED_SINCE = "connected_since";
	public static final String SWITCH_CAPABILITIES = "capabilities";
	public static final String SWITCH_BUFFERS = "buffers";
	public static final String SWITCH_TABLES = "tables";
	public static final String SWITCH_ACTIONS = "actions";


    // Names of table/fields for links in the storage API
	public static final String LINK_TABLE_NAME = "controller_link";
	public static final String LINK_ID = "id";
	public static final String LINK_SRC_SWITCH = "src_switch_id";
	public static final String LINK_SRC_PORT = "src_port";
    public static final String LINK_SRC_PORT_STATE = "src_port_state";
    public static final String LINK_DST_SWITCH = "dst_switch_id";
    public static final String LINK_DST_PORT = "dst_port";
    public static final String LINK_DST_PORT_STATE = "dst_port_state";
    public static final String LINK_VALID_TIME = "valid_time";
    public static final String LINK_TYPE = "link_type";

    public static void show(IStorageSourceService storageSource) {

        IResultSet link = storageSource.executeQuery(LINK_TABLE_NAME, null, null, null);
		int count = 0;
		List<String> linkIdList = new LinkedList<String>();

	    while (link.next()) {
	    	try {
				count++;
				logger.info("[ATTACK]-----------------------------------------------");
				logger.info("[ATTACK] LINK INFO FROM DB : Count = " + count);
				logger.info("[ATTACK]-----------------------------------------------");
				logger.info("[ATTACK] LINK_TABLE_NAME     = " + LINK_TABLE_NAME);
				logger.info("[ATTACK] LINK_ID             = " + link.getString(LINK_ID));
				logger.info("[ATTACK] LINK_SRC_SWITCH     = " + link.getString(LINK_SRC_SWITCH));
				logger.info("[ATTACK] LINK_SRC_PORT       = " + link.getString(LINK_SRC_PORT));
				logger.info("[ATTACK] LINK_SRC_PORT_STATE = " + link.getString(LINK_SRC_PORT_STATE));
				logger.info("[ATTACK] LINK_DST_SWITCH     = " + link.getString(LINK_DST_SWITCH));
				logger.info("[ATTACK] LINK_DST_PORT       = " + link.getString(LINK_DST_PORT));
				logger.info("[ATTACK] LINK_DST_PORT_STATE = " + link.getString(LINK_DST_PORT_STATE));
				logger.info("[ATTACK] LINK_VALID_TIME     = " + link.getString(LINK_VALID_TIME));
				logger.info("[ATTACK] LINK_TYPE           = " + link.getString(LINK_TYPE));
				logger.info("[ATTACK]-----------------------------------------------");
				logger.info("");
	       	} catch (NullPointerException e) {
				e.printStackTrace();
	    	}
	    }

    }

/*
		log.info("[ATTACK] Access InternalDBShow : delete Link Information");
		if (0 < count) {
			log.info("[ATTACK] delete Link Info: " + linkIdList.get(0));
			storageSource.deleteRow(LINK_TABLE_NAME, linkIdList.get(0));
		}
*/

/*
		log.info("[ATTACK] Access InternalDBShow : delete Link Information");

		String link_1 = new String("00:00:00:00:00:00:00:03-2-00:00:00:00:00:00:00:02-3");
		String link_2 = new String("00:00:00:00:00:00:00:02-3-00:00:00:00:00:00:00:03-2");

       	storageSource.deleteRow("controller_link", link_1);
       	storageSource.deleteRow("controller_link", link_2);
*/
/*

        log.info("[ATTACK] Access InternalDBShow : Link Information");
		String link_one= new String("00:00:00:00:00:00:00:03-2-00:00:00:00:00:00:00:02-3");

		IResultSet irs = storageSource.getRow(LINK_TABLE_NAME, link_one);
		Map<String,Object> row;
		Map<String,Object> row_abnormal = new HashMap<String, Object>();

		while (irs.next()) {
			row = irs.getRow();
			log.info("[ATTACK] LINK_ID             = " + irs.getString(LINK_ID));
			log.info("[ATTACK] LINK_SRC_SWITCH     = " + irs.getString(LINK_SRC_SWITCH));
			log.info("[ATTACK] LINK_SRC_PORT       = " + irs.getString(LINK_SRC_PORT));
			log.info("[ATTACK] LINK_SRC_PORT_STATE = " + irs.getString(LINK_SRC_PORT_STATE));
			log.info("[ATTACK] LINK_DST_SWITCH     = " + irs.getString(LINK_DST_SWITCH));
			log.info("[ATTACK] LINK_DST_PORT       = " + irs.getString(LINK_DST_PORT));
			log.info("[ATTACK] LINK_DST_PORT_STATE = " + irs.getString(LINK_DST_PORT_STATE));
			log.info("[ATTACK] LINK_VALID_TIME     = " + irs.getString(LINK_VALID_TIME));
			log.info("[ATTACK] LINK_TYPE           = " + irs.getString(LINK_TYPE));
//		} else {
//			log.info("[ATTACK] No Data for link: " + link_one);
		}

*/
/*
        log.info("[ATTACK] Access InternalDBShow : modify Switch Information");
		String switch_two = new String("00:00:00:00:00:00:00:02");
		String switch_abnormal = new String("00:00:00:00:00:00:00:10");

		IResultSet irs = storageSource.getRow(SWITCH_TABLE_NAME, switch_two);
		Map<String,Object> row;
		Map<String,Object> row_abnormal = new HashMap<String, Object>();

		if (irs.next()) {
			row = irs.getRow();
			log.info("[ATTACK] SWITCH_DATAPATH_ID     = " + irs.getString(SWITCH_DATAPATH_ID));
			log.info("[ATTACK] SWITCH_SOCKET_ADDRESS  = " + irs.getString(SWITCH_SOCKET_ADDRESS));
			log.info("[ATTACK] SWITCH_IP              = " + irs.getString(SWITCH_IP));
			log.info("[ATTACK] SWITCH_CONTROLLER_ID   = " + irs.getString(SWITCH_CONTROLLER_ID));
			log.info("[ATTACK] SWITCH_ACTIVE          = " + irs.getString(SWITCH_ACTIVE));
			log.info("[ATTACK] SWITCH_CONNECTED_SINCE = " + irs.getString(SWITCH_CONNECTED_SINCE));
			log.info("[ATTACK] SWITCH_CAPABILITIES    = " + irs.getString(SWITCH_CAPABILITIES));
			log.info("[ATTACK] SWITCH_BUFFERS         = " + irs.getString(SWITCH_BUFFERS));
			log.info("[ATTACK] SWITCH_TABLES          = " + irs.getString(SWITCH_TABLES));
			log.info("[ATTACK] SWITCH_ACTIONS         = " + irs.getString(SWITCH_ACTIONS));

			log.info("[ATTACK] modify switch_ip address");
			row_abnormal.put(SWITCH_DATAPATH_ID, switch_abnormal);
			row_abnormal.put(SWITCH_SOCKET_ADDRESS, "ATTACK_INVALID_SOCKET_ADDRESS");
			row_abnormal.put(SWITCH_IP,"ATTACK_INVALID_SWITCH_IP");
			row_abnormal.put(SWITCH_CONTROLLER_ID,"ATTACK_INVALID_CONTROLLER_ID");
			storageSource.insertRow(SWITCH_TABLE_NAME, row_abnormal);
		} else {
			log.info("[ATTACK] No Data for switch: " + switch_two);
		}

*/
}
