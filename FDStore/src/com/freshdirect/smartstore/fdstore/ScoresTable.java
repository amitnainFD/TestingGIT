package com.freshdirect.smartstore.fdstore;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jxl.Workbook;
import jxl.write.Blank;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * Table of scores.
 * 
 * @author istvan
 */
public abstract class ScoresTable implements Serializable {

	private static final long serialVersionUID = -1922989682790532518L;

	private List columnNames = new ArrayList();	
	private List columnTypes = new ArrayList();

	protected void init() {	
	}
	
	protected ScoresTable() {
		init();
	}
	
	protected void addColumn(String name, Class type) {
		columnNames.add(name);
		columnTypes.add(type);
	}
	/**
	 * Get column names for table.
	 * @return List<String>
	 */
	public List getColumnNames() {
		return columnNames;
	}
	
	/**
	 * Get column types.
	 * 
	 * @return List<{@link Class}>
	 */
	public List getColumnTypes() {
		return columnTypes;
	}
	
	/** Get number of columns.
	 * 
	 * @return number of columns
	 */
	public int columns() {
		return columnTypes.size();
	}
	
	/**
	 * Get value rows.
	 * 
	 * @return Iterator<List<Object>>
	 */
	public abstract Iterator getRows();
	
	
	/**
	 * Builds EXCEL formulas.
	 * 
	 * @author istvan
	 *
	 */
	protected class FormulaBuilder {
		
		
		private boolean dollar = false;
		
		private class Part {
			private String rep;
			
			protected Part(String rep) {
				this.rep = rep;
			}
			
			public String toString(int row) {
				return rep;
			}
		}
		
		private class Reference extends Part {
			private Reference(String rep) {
				super((dollar ? "$" : "") + rep);
			}
			
			public String toString(int row) {
				return super.toString(row) + row;
			}
		}
		
		private List parts = new ArrayList();
		
		protected String column(int ind) {
			StringBuffer chars = new StringBuffer();
			int base = 'Z' - 'A' + 1;
			++ind;
			do {
				--ind;
				chars.append((char)('A' + (ind % base)));
				ind /= base;
			} while(ind > 0);
			chars.reverse();
			return chars.toString();
		}
		
		protected FormulaBuilder(String s, boolean dollar) {
			s = s.trim();
			this.dollar = dollar;
			Map columnIndexes= new HashMap();
			Map columnReferences = new HashMap();
			int ind = 0;
			int occs = 0;
			for(Iterator i = getColumnNames().iterator(); i.hasNext();++ind) {
				String factorName = i.next().toString();
				String columnName = "${" + factorName + '}';
				columnReferences.put(columnName, column(ind));
				for(int p = 0;;) {
					p = s.indexOf(columnName, p);
					if (p == -1) {
						break;
					} else {
						List posList = (List)columnIndexes.get(columnName);
						if (posList == null) {
							posList = new ArrayList(2);
							columnIndexes.put(factorName, posList);
						}
						posList.add(new Integer(p));
						++occs;
					}
					p += columnName.length();
				}	
			}	
			if (occs == 0) {
				parts.add(new Part(s));
			} else {
				
				List refs = new ArrayList(occs);
				for(Iterator i = columnIndexes.entrySet().iterator(); i.hasNext();) {
					final Map.Entry entry = (Map.Entry)i.next();
					List values = (List)entry.getValue();
					for(Iterator j = values.iterator(); j.hasNext();) {
						final Integer p = (Integer)j.next();
						refs.add(new Map.Entry() {

							public Object getKey() {
								return p;
							}

							public Object getValue() {
								return entry.getKey();
							}

							public Object setValue(Object value) {
								return null;
							}
							
						});
					}					
				}
				Collections.sort(refs, 
					new Comparator() {
						public int compare(Object o1, Object o2) {
							int p1 = ((Number)(((Map.Entry)o1).getKey())).intValue();
							int p2 = ((Number)(((Map.Entry)o2).getKey())).intValue();
							return p1 - p2;
						}
					}
				);
				
				
				int l = 0;
				int f = 0;
				
				for(Iterator i = refs.iterator(); i.hasNext();) {
					Map.Entry entry = (Map.Entry)i.next();
					f = ((Number)entry.getKey()).intValue();
					
					if (f > l) {
						parts.add(new Part(s.substring(l, f)));
					}
					
					String ref = entry.getValue().toString();
					parts.add(new Reference(columnReferences.get("${" + ref + '}').toString()));
					l = f + ref.length() + 3;
				}
				
				if (l < s.length()) {
					parts.add(new Part(s.substring(l)));
				}
			}
		}
		
		public String toString(int row) {
			StringBuffer buffer = new StringBuffer();
			for(Iterator i = parts.iterator(); i.hasNext();) {
				buffer.append(((Part)i.next()).toString(row));
			}
			return buffer.toString();
		}
	}
	
	public String compileFormula(String formula, int row) {
		return new FormulaBuilder(formula,true).toString(row);
	}
	
	/**
	 * Write table in EXCEL.
	 * 
	 * The formulas are strings containing the equations in EXCEL format without the starting <tt>=</tt> sign
	 * and the columns can be referred to by name (with the syntax <tt>$<i>column</i></tt>).
	 * 
	 * @param os output stream
	 * @param formulas EXCEL formulas as strings
	 * @throws IOException
	 * @throws WriteException
	 */
	public void writeExcel(OutputStream os, List formulas) throws IOException, WriteException {
		
		List formulaBuilders = new ArrayList(formulas.size());
		for (Iterator i = formulas.iterator(); i.hasNext();) {
			String formula = i.next().toString();
			FormulaBuilder formulaBuilder = new FormulaBuilder(formula,false);
			formulaBuilders.add(formulaBuilder);
			System.out.println(formulaBuilder.toString(0));
		}
		
		WritableWorkbook workbook = Workbook.createWorkbook(os);
		WritableSheet sheet = workbook.createSheet("Data", 0);
		
		
		for(int j = 0; j< getColumnNames().size(); ++j ) {
			sheet.addCell(new Label(j,0,getColumnNames().get(j).toString()));
		}
		for(int j = 0; j< formulas.size(); ++j) {
			sheet.addCell(new Label(j+getColumnNames().size(),0,formulas.get(j).toString()));
		}
		
		int r = 1;
		for(Iterator i = getRows(); i.hasNext();++r) {
			List row = (List)i.next();
			for(int j = 0; j< row.size(); ++j) {
				Object obj = row.get(j);
				if (obj == null) {
					sheet.addCell(new Blank(j,r));
				} else {
					if (obj instanceof Number) {
						sheet.addCell(new jxl.write.Number(j,r,((Number)obj).doubleValue()));
					} else {
						sheet.addCell(new Label(j,r,obj.toString()));
					}
				}
			}
			for(int j=0; j< formulaBuilders.size(); ++j) {
				sheet.addCell(new Formula(j+row.size(),r,((FormulaBuilder)formulaBuilders.get(j)).toString(r+1)));
			}
		}
		workbook.write();
		workbook.close();
	}
	
	/**
	 * Same as {@link #writeExcel(OutputStream, List) writeExcel}(os,{@link Collections#EMPTY_LIST}).
	 * 
	 * @param os
	 * @throws IOException
	 * @throws WriteException
	 */
	public void writeExcel(OutputStream os) throws IOException, WriteException {
		writeExcel(os,Collections.EMPTY_LIST);
	}
}
