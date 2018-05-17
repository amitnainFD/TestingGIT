package com.freshdirect.fdstore.survey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.freshdirect.framework.util.StringUtil;

public class SurveyHtmlHelper {
	
	
	public static boolean hasActiveAnswers(FDSurveyQuestion question, List response) {
		
		if(response==null || response.isEmpty()) return false;
		List answers=question.getAnswers();
		if(answers==null || answers.isEmpty()) return false;
		boolean hasActiveAns=false;
		Iterator it=answers.iterator();
		List ansGroups=question.getAnswerGroups();

		while(!hasActiveAns && it.hasNext()) {
			FDSurveyAnswer _validAns=(FDSurveyAnswer)it.next();//
			if((EnumFormDisplayType.GROUPED_RADIO_BUTTON.equals(question.getFormDisplayType())||EnumFormDisplayType.DISPLAY_PULLDOWN_GROUP.equals(question.getFormDisplayType())) && response.contains(_validAns.getName())) {
				hasActiveAns=true;
			}else if(ansGroups.size()==0 && response.contains(_validAns.getName())) {
				hasActiveAns=true;
			}else if(ansGroups.size()>0){
				Iterator _it=ansGroups.iterator();
				while(!hasActiveAns&& _it.hasNext()) {
					String ansGroup=_it.next().toString();
					if(response.contains(_validAns.getName()+ansGroup)) {
						hasActiveAns=true;
					}
				}
			}
		}
		return hasActiveAns;
	}
	
	
	public static String getQuestionText(FDSurveyQuestion quest) {
		
		if(FDSurveyConstants.PROFILE.equals(quest.getName()))
				return quest.getDescription();
		if(EnumFormDisplayType.GROUPED_RADIO_BUTTON.equals(quest.getFormDisplayType()))
			return quest.getDescription()+FDSurveyConstants.PAIRED_CHOICE;
		return quest.isPulldown()? quest.getDescription():quest.isMultiselect()?quest.getDescription()+FDSurveyConstants.MULTIPLE_CHOICE:quest.getDescription()+FDSurveyConstants.SINGLE_CHOICE;
	}
	public static String getAnswers(FDSurveyQuestion question, List answers) {
		
		
		EnumViewDisplayType view=question.getViewDisplayType();
		if ((view==null)||(answers==null)||answers.isEmpty())
			return "";
		
		StringBuffer response=new StringBuffer(200);
		
		if(EnumViewDisplayType.SINGLE_ANS_PER_ROW.equals(view)) {
			for (int i=0;i<answers.size();i++) {
				
				response.append(getRow(getAnsDesc(answers.get(i).toString(),question.getAnswers())));
		    }
			return response.toString();
		} else if(EnumViewDisplayType.NUMBERED_LIST.equals(view)) {
			for (int i=0;i<answers.size();i++) {
				
				response.append(getRow((i+1)+" "+answers.get(i).toString()));
		    }
			return response.toString();
		} else if (EnumViewDisplayType.GROUPED_COMMA_SEPARATED.equals(view)) {
			Collections.sort(answers);
			List ansGroup=question.getAnswerGroups();
			Collections.sort(ansGroup);
			String _ansGroup="";
			for(int i=0;i<ansGroup.size();i++) {
				StringBuffer temp=new StringBuffer(300);
				_ansGroup=ansGroup.get(i).toString();
				//temp.append("<DIV style=\"font-weight:bold\" >"+_ansGroup+": </DIV>");
				temp.append("<b>"+_ansGroup+": </b>");
				String _ans="";
				boolean needsDisplay=false;
				for(int j=0;j<answers.size();j++) {
					_ans=answers.get(j).toString();
					if(_ans.indexOf(_ansGroup)!=-1) {
						temp.append(getAnsDesc(_ans.substring(0, _ans.indexOf(_ansGroup)),question.getAnswers())).append(", ");
						needsDisplay=true;
					}
				}
				if(needsDisplay) {
					response.append(getRow(temp.substring(0, temp.length()-2)));
				}
				
			}
			return response.toString();
		} else if(EnumViewDisplayType.COMMA_SEPARATED.equals(view)) {
			Collections.sort(answers);
			StringBuffer temp=new StringBuffer(300);
			for(int j=0;j<answers.size();j++) {
				temp.append(getAnsDesc(answers.get(j).toString(),question.getAnswers())).append(", ");
			}
			response.append(getRow(temp.substring(0, temp.length()-2)));
			return response.toString();
		} else if(EnumViewDisplayType.GROUPED_LIST.equals(view)) {
			
			List ansGroup=question.getAnswerGroups();
			String _ansGroup="";
			for(int i=0;i<ansGroup.size();i++) {
				StringBuffer temp=new StringBuffer(300);
				_ansGroup=ansGroup.get(i).toString();
				
				temp.append("<b>"+_ansGroup+": </b>");
				String _ans="";
				boolean needsDisplay=false;
				if(_ansGroup.indexOf(" ")!=-1) {
					_ansGroup=_ansGroup.substring(0, _ansGroup.indexOf(" "));
				}
				for(int j=0;j<answers.size();j++) {
					_ans=answers.get(j).toString();
					if(_ans.indexOf(_ansGroup)!=-1) {
						temp.append(getAnsDesc(_ans,question.getAnswers())).append(FDSurveyConstants.MEAL_COUNT_SUMMARY);
						needsDisplay=true;
					}
				}
				if(needsDisplay) {
					response.append(getRow(temp.substring(0, temp.length())));
				}
				
			}
			return response.toString();		
		}
		
		return "";
	}
	
	private static String getAnsDesc(String ansId, List answers) {
		
		if (StringUtil.isEmpty(ansId)||answers==null || answers.isEmpty()) return "";
		if(FDSurveyConstants.NONE.equals(ansId)) return FDSurveyConstants.NONE;
		
		boolean match=false;
		Iterator it=answers.iterator();
		String desc="";
		while(!match && it.hasNext()) {
			FDSurveyAnswer ans=(FDSurveyAnswer)it.next();
			if (ansId.equals(ans.getName())) {
				match=true;
				desc=ans.getDescription();
			}
		}
		return desc; 
	}
	private static String getRow(String data) {
		
		StringBuffer response=new StringBuffer(400);
		response.append("<tr><td style=\"padding-bottom: 6px;\">\n");
		response.append(data);
		response.append("</td>\n");
		response.append("</tr>\n");
		return response.toString();
	}

	public static final String getAnswersHtml(String id,FDSurveyQuestion question, List  previousAnswers) {
		
		StringBuffer response = new StringBuffer(200);
		if(previousAnswers==null)
			previousAnswers=new ArrayList();
		if (question.isPulldown()) {
			if(EnumFormDisplayType.DISPLAY_PULLDOWN_GROUP.equals(question.getFormDisplayType())) {
				return getPulldownHtml(question,previousAnswers,true);
			} else {
				return getPulldownHtml(question,previousAnswers,false);
			}
		} else {
			if (EnumFormDisplayType.SINGLE_ANS_PER_ROW.equals(question
					.getFormDisplayType())) {

	                        List<String> displayElements = getDisplayElements(id, question,
                                        previousAnswers);
                        
				String data = "";
				for (int i = 0; i < displayElements.size(); i++) {
					data = displayElements.get(i);
					if (displayElements.size() <=4) {
						response.append(getDivTag(getRowStyle(0), "", data));
					} else {
						response.append(getDivTag(getRowStyle(i + 1), "", data));
					}
				}
			} else if (EnumFormDisplayType.TWO_ANS_PER_ROW.equals(question
					.getFormDisplayType())) {
	                        List<String[]> displayElements = getDisplayElementsPairs(id, question,
                                        previousAnswers);
                        
				int ansCount = displayElements.size();
				StringBuffer tmp1 = new StringBuffer(200);
				StringBuffer tmp2 = new StringBuffer(200);
				int rowStyle = 1;
				boolean oddRow = (ansCount %2 == 0 && ansCount !=0)?false:true;
				for (int i = 0; i < ansCount; i++) {
					String[] data = displayElements.get(i);
					String rStyle = getRowStyle(rowStyle);
					if (i % 2 == 0)
						tmp1.append(getDivTag(rStyle, "", wrapText(data, rStyle)));
					else {
						tmp2.append(getDivTag(rStyle, "", wrapText(data, rStyle)));
						rowStyle=(rowStyle == 0)?1:0;
					}
					if (i== ansCount-1 && oddRow) {
						tmp2.append(getDivTag(getRowStyle(rowStyle), "", "<nobr>&nbsp;</nobr>"));
					}
				}

				response.append(getDivTag("q05_container q05_colwfixed", "", tmp1
						.toString()));
				response.append(getDivTag("q05_container q05_colwfixed", "", tmp2
						.toString()));

			} else if(EnumFormDisplayType.GROUPED_RADIO_BUTTON.equals(question.getFormDisplayType())) {
				return getGroupedRadio(question,previousAnswers);
			}else if(EnumFormDisplayType.GROUPED_MULTI_SELECTION.equals(question.getFormDisplayType())) {
				return getGroupedSelection(question,previousAnswers);
			}else if(EnumFormDisplayType.IMAGE_DISPLAY.equals(question.getFormDisplayType())) {
				return getImageDisplay(question,previousAnswers);
			} else {
			    throw new RuntimeException("question: "+question.getName()+" form display:"+question.getFormDisplayType());
			}
		}
		return response.toString();
	}

	static String wrapText(String[] data, String style) {
		return "<div class=\""+style+"\" style=\"line-height:22px; width: 100%;\">"+data[0]+"&nbsp;"+data[1]+"</div>";
	}
	
	static String wrapText(String data, boolean nobr) {
	    return nobr ? "<nobr>" + data + "&nbsp;</nobr>" : "<span class=\"surveyAnswer\">" + data + "</span>";
	}
	
	
	
	public static final String getAnswersHtml(String id,
			FDSurveyQuestion question, FDSurveyResponse previousResponse) {

		
		List previousAnswers = getPreviousAnswers(previousResponse, question);
		return getAnswersHtml(id,question, previousAnswers);
	}

    private static String getImageDisplay(FDSurveyQuestion question, List previousAnswers) {
		StringBuffer response=new StringBuffer(200);
		List answers=question.getAnswers();
		FDSurveyAnswer answer=null;
		//
		response.append("\n<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" align=\"center\">\n<tr>\n");
		//
		int colCount = 0;
		for(int i=0;i<answers.size();i++) {
			answer=(FDSurveyAnswer)answers.get(i);
			StringBuffer temp=new StringBuffer(200);
            String input = getInputTag(FDSurveyConstants.SINGLE_SELECT_INPUT, question.getName(), "", answer.getName(), previousAnswers.contains(answer.getName()), false, "");
			temp.append("<td width=\"16%\" align=\"center\">\n");
			temp.append(getDivTag(getImageTag(answer.getDescription())));
            temp.append(wrapLabel(input + wrapOffscreen(answer.getName())));
			temp.append("</td>");
			colCount++;
			if (colCount == 6 && i != answers.size()-1) {
				temp.append("</tr>\n<tr>\n<td style=\"height:8px;\" colspan=\"6\">&nbsp;</td>\n</tr>\n");
				temp.append("<tr>\n");
				colCount = 0;
			}
			response.append(temp.toString());
		}
		//
		response.append("</tr></table>\n");
		//
		return response.toString();
	}

    private static String wrapOffscreen(String content) {
        return "<span class='offscreen'>" + content + "</span>";
    }

    private static String wrapLabel(String content) {
        return "<label>" + content + "</label>";
    }

    private static String getImageTag(String path) {
    	return "<img src=\""+path+"\"/>";
    }
	private static String getGroupedSelection(FDSurveyQuestion question, List previousAnswers) {
		StringBuffer response=new StringBuffer(200);
		List answerGroups=question.getAnswerGroups();
		Collections.sort(answerGroups);
    	if(answerGroups==null || answerGroups.size()==0)
    		return "";
    	List answers=question.getAnswersByGroup(answerGroups.get(0).toString());
    	response.append(getGroupedSelectionHeader(answerGroups));
    	FDSurveyAnswer answer=null;
		for(int i=0;i<answers.size();i++) {
			StringBuffer temp=new StringBuffer(200);
			answer=(FDSurveyAnswer)answers.get(i);
			temp.append(getDivTag(("q08_text ")+getRowStyle(i + 1),"",answer.getDescription()));
			for(int j=0;j<answerGroups.size();j++) {
				String answerGroup=answerGroups.get(j).toString();
				String value = answer.getName()+answerGroup;
				boolean checked = previousAnswers.contains(answer.getName()+answerGroup) ? true: false;
                String input = getInputTag(FDSurveyConstants.MULTI_SELECT_INPUT, question.getName() + FDSurveyConstants.NAME_SEPERATOR + answerGroup, "", value, checked, false, "");
                temp.append(getDivTag("q08_cb", "", wrapLabel(input + wrapOffscreen(answer.getName() + " " + answerGroup))));
			}
			temp.append(getDivTag("cboth","","<!--  -->"));
			response.append(getDivTag(getRowStyle(i + 1),"",temp.toString()));
		}
		return response.toString();
	}

	private static String getGroupedSelectionHeader(List answerGroups) {
		StringBuffer response=new StringBuffer(200);
		response.append(getDivTag("q08_text","","<!-- header -->"));
		for(Iterator it=answerGroups.iterator();it.hasNext();) {
			response.append(getDivTag("q08_cb","",it.next().toString()));
		}
		response.append(getDivTag("cboth","","<!--  -->"));
		return response.toString();
	}

	private static String getGroupedRadio(FDSurveyQuestion question, List previousAnswers) {
    	StringBuffer response=new StringBuffer(200);
    	List answerGroups=question.getAnswerGroups();
    	String group="";
    	int counter=0;
    	int colCount = 0;
    	int colTotalCount = 0;
    	// number of rows to be painted whole
    	int rowCountWhole = answerGroups.size()/3;
    	String lastRow = rowCountWhole%2==0 ? "odd":"even";
    	int rowCount = 0;
    	int colToAdd = 3 - (answerGroups.size()%3);
    	boolean fullGrid = colToAdd == 3?true:false;
		for(Iterator it=answerGroups.iterator();it.hasNext();) {
			StringBuffer temp=new StringBuffer(200);
			group=it.next().toString();
			List answers=question.getAnswersByGroup(group);
			FDSurveyAnswer answer=null;
			
			for(int i=0;i<answers.size();i++) {
				
				answer=(FDSurveyAnswer)answers.get(i);
				String value = answer.getName();
				boolean checked = previousAnswers.contains(value) ? true: false;
				temp.append(getInputTag(FDSurveyConstants.SINGLE_SELECT_INPUT, question.getName()+FDSurveyConstants.NAME_SEPERATOR+group, "", value, checked, false, ""));
				temp.append(answer.getDescription());
				if (i==0 || i%2 == 0) {
					temp.append("<br>");
				}
				//temp.append(getDivTag("q09_rb","",getInputTag(FDSurveyConstants.SINGLE_SELECT_INPUT, question.getName()+FDSurveyConstants.NAME_SEPERATOR+group, "", value, checked, false, "")));
				//temp.append(getDivTag("q09_text","",answer.getDescription()));
				
			}
			colTotalCount++;
			String container=(counter%2==0)?"q09_container even":"q09_container odd";
			//System.out.println(">>>>>>>>>>>>>"+counter+" "+colCount + " total " + rowCountWhole  );
			if (colCount == 0) {
				if (rowCount == rowCountWhole || (fullGrid && rowCount == rowCountWhole-1) ) {
					container += " leftBL";
				} else {
					container += " leftB";
				}
			} else if (colCount == 1){
				if (rowCount == rowCountWhole || (fullGrid && rowCount == rowCountWhole-1) ){
					container += " middleBL";
				} else {
					container += " middleB";
				}
			} else if (colCount == 2){
				if (rowCount == rowCountWhole  || (fullGrid && rowCount == rowCountWhole-1) ){
					container += " rightBL";
				} else {
					container += " rightB";
				}
				rowCount++;
			}

			response.append(getDivTag(container,"",temp.toString()));
			//response.append(getDivTag(container,"",getDivTag("box","",temp.toString())));
			if (colCount == 2) {
				colCount = 0;
			} else {
				colCount++;
			}
			counter++;
		}
		//paint the last divs to close up 'table'
		if (colToAdd == 1) {
			response.append(getDivTag("q09_container "+("even".equals(lastRow)?"odd":"even")+" rightBL","","<br><br><br>"));
		} else if (colToAdd == 2){
			response.append(getDivTag("q09_container "+("even".equals(lastRow)?"even":"odd")+" middleBL","","<br><br><br>"));
			response.append(getDivTag("q09_container "+("even".equals(lastRow)?"odd":"even")+" rightBL","","<br><br><br>"));
		}
		
		return response.toString();
    }
    
	private static String getPulldownHtml(FDSurveyQuestion question, List previousAnswers, boolean group) {

		StringBuffer response = new StringBuffer(200);
		List answerGroups=question.getAnswerGroups();
		String _group="";
		int countGroup = 0;
		if(answerGroups!=null && answerGroups.size()>0) {
			for(Iterator it=answerGroups.iterator();it.hasNext();) {
				Object obj=it.next();
				if(obj!=null)
					_group=obj.toString();
				// multi response
				if(group) {
					// meals question style
					response.append("<label>" + getSelectTag(question,_group,previousAnswers,"50","--")+ "&nbsp;&nbsp;"+ getSpanTag(_group)+"</label><br/>");
					countGroup++;
					if (countGroup-1 != answerGroups.size()) {
						response.append("<br/>");
					}
				} else {
					// birthday style
					if (countGroup == 0) {
						response.append(getSelectTag(question,_group,previousAnswers,"90","MONTH"));
					} else {
						response.append(getSelectTag(question,_group,previousAnswers,"60","DAY"));
					}
					countGroup++;
				}
			}
		} else {
			// single response
			response.append(getSelectTag(question,"",previousAnswers,"",""));
		}
		return response.toString();
	}

	private static String getPulldownHtml(FDSurveyQuestion question,String group, List previousAnswers) {

		StringBuffer response = new StringBuffer(200);
		List answerGroups=question.getAnswerGroups();
		for(Iterator it=answerGroups.iterator();it.hasNext();) {
			response.append(getSelectTag(question,it.next().toString(),previousAnswers,"",""));
		}
		return response.toString();
	}

	private static String getSpanTag(String data) {
		return "<span>"+data+"</span>";
	}
	private static String getSelectTag(FDSurveyQuestion question,String group, List selectedValues,String width, String chooseText) {
		// Pulldown rendering
		StringBuffer response=new StringBuffer(200);
		List answers=null;
		if("".equals(width)) width="160";
		if("".equals(chooseText)) chooseText="Please choose one";
		if(!"".equals(group))
			answers=question.getAnswersByGroup(group);
		else
			answers=question.getAnswers();
		response.append("<select name=\"").append(question.getName()).append("\" style=\"width:"+width+"px;\" class=\"surveyText\">");
		response.append("<option value=\"\" selected=\"selected\">"+chooseText+"</option>");
		FDSurveyAnswer answer=null;
		for(int i=0;i<answers.size();i++) {
			
			answer=(FDSurveyAnswer)answers.get(i);
			response.append("<option value=\"").append(answer.getName()).append("\" ");
			if(selectedValues!=null && !selectedValues.isEmpty()&& selectedValues.contains(answer.getName())) {
				response.append(" SELECTED ");
			}
			response.append(">").append(answer.getDescription()).append("</option>");
		}
		response.append("</select>");
		return response.toString();
	}

	private static boolean isNoneOption(String answer) {
		return (FDSurveyConstants.NONE.equalsIgnoreCase(answer));
	}

	private static boolean isOtherOption(String answer) {
		return (FDSurveyConstants.OTHER.equalsIgnoreCase(answer));
	}


	private static String getInputTag(String type, String name,
			String styleName, String value, boolean checked, boolean disabled,
			String script) {

		StringBuffer response = new StringBuffer(200);
		response.append("<input ");
		if (!StringUtil.isEmpty(script)) {
			response.append(script);
		}
		if (checked)
			response.append(FDSurveyConstants.CHECKED);
		if (disabled)
			response.append(FDSurveyConstants.DISABLED);
		if (!StringUtil.isEmpty(styleName)) {
			response.append(" class=\"").append(styleName).append("\" ");
		}

		response.append(" type=\"").append(type).append("\" name=\"").append(
				StringUtil.escapeHTML(name)).append("\" value=\"")
				.append(value).append("\" />");
		return response.toString();
	}

	private static String getDivTag(String className, String id, String data) {
		StringBuffer response = new StringBuffer(200);
		response.append("\n<div");
		if (!StringUtil.isEmpty(className)) {
			response.append(" class=\"").append(className).append("\"");
		}
		if (!StringUtil.isEmpty(id)) {
			response.append(" id=\"").append(id).append("\"");
		}
		response.append(">").append(data).append("</div>\n");
		return response.toString();
	}
	
	private static String getDivTag(String className, String id, String data, String divStyle) {
		StringBuffer response = new StringBuffer(200);
		response.append("\n<div");
		if (!StringUtil.isEmpty(className)) {
			response.append(" class=\"").append(className).append("\"");
		}
		if (!StringUtil.isEmpty(id)) {
			response.append(" id=\"").append(id).append("\"");
		}
		if (!StringUtil.isEmpty(divStyle)) {
			response.append(" style=\"").append(divStyle).append("\"");
		}
		response.append(">").append(data).append("</div>\n");
		return response.toString();
	}

	private static String getDivTag(String data) {

		return "<div>" + data + "</div>";
	}

	private static List getCustomResponse(List previousAnswers,
			Collection probableAnswers) {

		if (previousAnswers.isEmpty())
			return null;
		List validAnswers = new ArrayList(probableAnswers.size());
		for (Iterator it = probableAnswers.iterator(); it.hasNext();) {
			FDSurveyAnswer answer = (FDSurveyAnswer) it.next();
			validAnswers.add(answer.getName());
		}
		List customResponse = new ArrayList();
		String _ans = "";
		for (int i = 0; i < previousAnswers.size(); i++) {
			_ans = previousAnswers.get(i).toString();
			if (!validAnswers.contains(_ans)) {
				customResponse.add(_ans);
			}
		}
		return customResponse;
	}


	private static List<String> getDisplayElements(String id,
			FDSurveyQuestion question, List previousAnswers) {
		List displayElements = new ArrayList(question.getAnswers().size());
		Iterator it = question.getAnswers().iterator();
		FDSurveyAnswer answer = null;

		boolean disable = isNoneOptionSelected(previousAnswers);
		List customResponseList = getCustomResponse(previousAnswers, question
				.getAnswers());
		while (it.hasNext()) {
			answer = (FDSurveyAnswer) it.next();
			String value = answer.getName();
			String input = FDSurveyConstants.SINGLE_SELECT_INPUT;
			boolean checked = previousAnswers.contains(value) ? true: false;
			if (question.isMultiselect()) {
				input = FDSurveyConstants.MULTI_SELECT_INPUT;
			}
			
			if (isOtherOption(answer.getName())) {
				if (customResponseList != null && !customResponseList.isEmpty())
					value = customResponseList.remove(0).toString();
				else
					value = "";
				displayElements.add(answer.getDescription()
						+ getInputTag(FDSurveyConstants.TEXT_INPUT,
								question.getName(),
								FDSurveyConstants.OTHER_INPUT_STYLE, value,
								false, disable, ""));
			} else if (isNoneOption(answer.getName())) {
				String script = "onclick=\" " + FDSurveyConstants.CLEAR_SCRIPT
						+ "(\'" + id + "\',this.checked)\" ";
				displayElements.add(getInputTag(input, question
						.getName(), FDSurveyConstants.NONE_INPUT_STYLE, value,
						checked, false, script)
						+ "&nbsp;" + answer.getDescription());
				/*
				displayElements.add(getDivTag(getInputTag(input, question
						.getName(), FDSurveyConstants.NONE_INPUT_STYLE, value,
						checked, false, script))
						+ answer.getDescription());
				*/
			} else {
				displayElements.add(getInputTag(input, question
						.getName(), "", value, checked, disable, "")
						+ "&nbsp;" + answer.getDescription());
				/*
				displayElements.add(getDivTag(getInputTag(input, question
						.getName(), "", value, checked, disable, ""))
						+ answer.getDescription());
				*/
			}
		}
		return displayElements;

	}


        private static List<String[]> getDisplayElementsPairs(String id,
                FDSurveyQuestion question, List previousAnswers) {
        List<String[]> displayElements = new ArrayList<String[]>(question.getAnswers().size());
        Iterator it = question.getAnswers().iterator();
        FDSurveyAnswer answer = null;

        boolean disable = isNoneOptionSelected(previousAnswers);
        List customResponseList = getCustomResponse(previousAnswers, question
                        .getAnswers());
        while (it.hasNext()) {
                answer = (FDSurveyAnswer) it.next();
                String value = answer.getName();
                String input = FDSurveyConstants.SINGLE_SELECT_INPUT;
                boolean checked = previousAnswers.contains(value) ? true: false;
                if (question.isMultiselect()) {
                        input = FDSurveyConstants.MULTI_SELECT_INPUT;
                }
                String[] res = new String[2];
                res[1] = answer.getDescription();
                
                if (isOtherOption(answer.getName())) {
                        if (customResponseList != null && !customResponseList.isEmpty())
                                value = customResponseList.remove(0).toString();
                        else
                                value = "";
                        res[0] = getInputTag(FDSurveyConstants.TEXT_INPUT,
                                question.getName(),
                                FDSurveyConstants.OTHER_INPUT_STYLE, value,
                                false, disable, "");
                        
                        displayElements.add(res);
                } else if (isNoneOption(answer.getName())) {
                        String script = "onclick=\" " + FDSurveyConstants.CLEAR_SCRIPT
                                        + "(\'" + id + "\',this.checked)\" ";
                        
                        res[0] = getInputTag(input, question
                                .getName(), FDSurveyConstants.NONE_INPUT_STYLE, value,
                                checked, false, script);
                        displayElements.add(res);
                } else {
                    res[0] = getInputTag(input, question
                            .getName(), "", value, checked, disable, "");
                        displayElements.add(res);
                }
        }
        return displayElements;

}
	
	

	private static boolean isNoneOptionSelected(List previousAnswers) {
		return previousAnswers.contains(FDSurveyConstants.NONE);
	}


	private static String getRowStyle(int rowIndex) {
		return (rowIndex % 2 == 0)? FDSurveyConstants.EVEN_ROW_STYLE: FDSurveyConstants.ODD_ROW_STYLE;
	}

	private static List getPreviousAnswers(FDSurveyResponse previousResponse,
			FDSurveyQuestion question) {
		return previousResponse != null?previousResponse.getAnswerAsList(question.getName()):new ArrayList();
	}
	
	       public static int getResponseCoverage(FDSurvey survey, FDSurveyResponse response) {
	                
	                if(survey==null || response==null)
	                        return 0;
	                
	                if(survey.getQuestions()==null || survey.getQuestions().isEmpty()||response.getAnswers()==null || response.getAnswers().isEmpty())
	                        return 0;
	                
	                int responseCount=0;
	                FDSurveyQuestion question=null;
	                for (Iterator it=survey.getQuestions().iterator();it.hasNext();) {
	                        question=(FDSurveyQuestion)it.next();
	                        if(response.getAnswers().containsKey(question.getName())) {
	                                
	                                if(hasActiveAnswers(question,response.getAnswerAsList(question.getName())))
	                                        responseCount++;
	                        }
	                }
	                return (responseCount*100)/survey.getQuestions().size();
	        }

}
