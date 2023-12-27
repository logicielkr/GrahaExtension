/*
 *
 * Copyright (C) HeonJik, KIM
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package kr.graha.app.command;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import kr.graha.post.interfaces.Processor;
import kr.graha.post.lib.Record;
import java.util.logging.Logger;
import java.util.logging.Level;
import kr.graha.helper.LOG;
import kr.graha.helper.DB;
import java.sql.SQLException;
import java.sql.Connection;

/**
 * 정렬순서를 적용한다.
 * 
 * @author HeonJik, KIM
 
 * @see kr.graha.post.interfaces.Processor;
 
 * @version 0.9
 * @since 0.9
 */
public class ApplyOrderNumberProcessorImpl implements Processor {
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
	public ApplyOrderNumberProcessorImpl() {
		
	}

/**
 * Graha 가 호출하는 메소드
 * 정렬순서를 변경한다.
 
 * @param request HttpServlet 에 전달된 HttpServletRequest
 * @param response HttpServlet 에 전달된 HttpServletResponse
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param con 데이타베이스 연결(Connection)

 * @see javax.servlet.http.HttpServletRequest (Apache Tomcat 10 미만)
 * @see jakarta.servlet.http.HttpServletRequest (Apache Tomcat 10 이상)
 * @see javax.servlet.http.HttpServletResponse (Apache Tomcat 10 미만)
 * @see jakarta.servlet.http.HttpServletResponse (Apache Tomcat 10 이상)
 * @see kr.graha.post.lib.Record 
 * @see java.sql.Connection 
 */

	public void execute(HttpServletRequest request, HttpServletResponse response, Record params, Connection con) {
		String sql = "update " + params.getString(Record.key(Record.PREFIX_TYPE_PROP, "table.name")) + " set " + params.getString(Record.key(Record.PREFIX_TYPE_PROP, "table.order_column")) + " = ?";
		sql += " where " + params.getString(Record.key(Record.PREFIX_TYPE_PROP, "table.pk")) + " = ?";
		int whereColumnCount = 0;
		if(params.hasKey(Record.key(Record.PREFIX_TYPE_PROP, "table.where_column.count"))) {
			whereColumnCount = params.getInt(Record.key(Record.PREFIX_TYPE_PROP, "table.where_column.count"));
		}
		for(int i = 0; i < whereColumnCount; i++) {
			sql += " and " + params.getString(Record.key(Record.PREFIX_TYPE_PROP, "table.where_column.name." + i)) + " = ?";
		}
		Object[] param = new Object[whereColumnCount + 2];
		try {
			int index = 1;
			while(true) {
				String pkColumnName = params.getString(Record.key(Record.PREFIX_TYPE_PROP, "table.pk"));
				String orderColumnName = params.getString(Record.key(Record.PREFIX_TYPE_PROP, "table.order_column"));
				if(params.hasKey(Record.key(Record.PREFIX_TYPE_PARAM, pkColumnName + "." + index)) && params.hasKey(Record.key(Record.PREFIX_TYPE_PARAM, orderColumnName + "." + index))) {
					param[0] = params.getIntObject(Record.key(Record.PREFIX_TYPE_PARAM, orderColumnName + "." + index));
					param[1] = params.getIntObject(Record.key(Record.PREFIX_TYPE_PARAM, pkColumnName + "." + index));
					for(int i = 0; i < whereColumnCount; i++) {
						String valueParamName = params.getString(Record.key(Record.PREFIX_TYPE_PROP, "table.where_column.value." + i));
						if(params.equals(Record.key(Record.PREFIX_TYPE_PROP, "table.where_column.datatype." + i), "int")) {
							param[i + 2] = params.getIntObject(Record.key(Record.PREFIX_TYPE_UNKNOWN, valueParamName));
						} else if(params.equals(Record.key(Record.PREFIX_TYPE_PROP, "table.where_column.datatype." + i), "float")) {
							param[i + 2] = params.getFloatObject(Record.key(Record.PREFIX_TYPE_UNKNOWN, valueParamName));
						} else if(params.equals(Record.key(Record.PREFIX_TYPE_PROP, "table.where_column.datatype." + i), "double")) {
							param[i + 2] = params.getDoubleObject(Record.key(Record.PREFIX_TYPE_UNKNOWN, valueParamName));
						} else if(params.equals(Record.key(Record.PREFIX_TYPE_PROP, "table.where_column.datatype." + i), "long")) {
							param[i + 2] = params.getLongObject(Record.key(Record.PREFIX_TYPE_UNKNOWN, valueParamName));
						} else if(params.equals(Record.key(Record.PREFIX_TYPE_PROP, "table.where_column.datatype." + i), "boolean")) {
							param[i + 2] = Boolean.valueOf(params.getBoolean(Record.key(Record.PREFIX_TYPE_UNKNOWN, valueParamName)));
						} else if(
							params.equals(Record.key(Record.PREFIX_TYPE_PROP, "table.where_column.datatype." + i), "date") &&
							params.hasKey(Record.key(Record.PREFIX_TYPE_PROP, "table.where_column.pattern." + i))
						) {
							param[i + 2] = params.getDate(
								Record.key(Record.PREFIX_TYPE_UNKNOWN, valueParamName),
								params.getString(Record.key(Record.PREFIX_TYPE_PROP, "table.where_column.pattern." + i))
							);
						} else if(
							params.equals(Record.key(Record.PREFIX_TYPE_PROP, "table.where_column.datatype." + i), "timestamp") &&
							params.hasKey(Record.key(Record.PREFIX_TYPE_PROP, "table.where_column.pattern." + i))
						) {
							param[i + 2] = params.getTimestamp(
								Record.key(Record.PREFIX_TYPE_UNKNOWN, valueParamName),
								params.getString(Record.key(Record.PREFIX_TYPE_PROP, "table.where_column.pattern." + i))
							);
						} else {
							param[i + 2] = params.getString(Record.key(Record.PREFIX_TYPE_UNKNOWN, valueParamName));
						}
					}
					if(DB.execute(con, null, sql, param) == 0) {
						params.put(Record.key(Record.PREFIX_TYPE_ERROR, "error"), "message.20001");
						break;
					}
				} else {
					break;
				}
				index++;
			}
		} catch (SQLException e) {
			params.put(Record.key(Record.PREFIX_TYPE_ERROR, "error"), "message.20002");
			if(logger.isLoggable(Level.SEVERE)) { logger.severe(LOG.toString(e)); }
		}
	}
}
