package com.freshdirect.fdstore.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletResponse;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.content.CategoryModel;
import com.freshdirect.fdstore.content.Domain;
import com.freshdirect.fdstore.content.DomainValue;
import com.freshdirect.fdstore.content.ProductContainer;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.view.ProductRating;
import com.freshdirect.fdstore.content.view.WebProductRating;

public class RatingUtil {
	
	public static WebProductRating getRatings(ProductModel productNode) throws FDResourceException {
		
		WebProductRating webRating = null;
		List<String> prodPageRatingNames = new ArrayList<String>();
                List<String> prodPageTextRatingNames = new ArrayList<String>();
		if (productNode.getProdPageRatings()!=null) {
                    StringTokenizer stRN=new StringTokenizer(productNode.getProdPageRatings(),",");
                    while (stRN.hasMoreTokens()) {
                        prodPageRatingNames.add(stRN.nextToken().toLowerCase());
                    }
		}
		if (productNode.getProdPageTextRatings()!=null) {
                    StringTokenizer stRN=new StringTokenizer(productNode.getProdPageTextRatings(),",");
                    while (stRN.hasMoreTokens()) {
                        prodPageTextRatingNames.add(stRN.nextToken().toLowerCase());
                    }
		}

		CategoryModel currentFolder = (CategoryModel)productNode.getParentNode();
		String rrFolderLabel= null; //"Compare "+currentFolder.getFullName();
		String rrFolderId = null;   //"catId="+currentFolder.getContentName();

		CategoryModel cat = currentFolder.getRatingHome();
                if (cat != null) {
//                    if (cr instanceof DepartmentModel) {
//                        DepartmentModel dep = (DepartmentModel) cr;
//                        rrFolderLabel = "Compare " + dep.getFullName();
//                        rrFolderId = "deptId=" + dep.getContentKey().getId();
//                    } else if (cr instanceof CategoryModel) {
//                        CategoryModel cat = (CategoryModel) cr;
                        rrFolderLabel = "Compare " + cat.getFullName();
                        rrFolderId = "catId=" + cat.getContentKey().getId();
//                    } else {
//                        rrFolderLabel = null;
//                    }
                }

		String ratGroupName  = currentFolder.getRatingGroupNames();
		StringBuffer rateNRankLinks =null;

		if (ratGroupName !=null && ratGroupName.trim().length() > 0 && rrFolderLabel !=null) {
                    rateNRankLinks = new StringBuffer();
                    StringTokenizer stRRNames = new StringTokenizer(ratGroupName,",");
                    String rrName = stRRNames.nextToken().toUpperCase();
                    String ordrBy = "&orderBy=name";
                    // go find the attribute with that name and it's label
                    List<Domain> ratings = currentFolder.getRatingDomain(rrName);
                    if (ratings!=null && ratings.size() > 0) {
                        Domain raDMV = (Domain) ratings.get(0);
                        ordrBy = "&orderBy="+raDMV.getName().toLowerCase();
                    }
                    rateNRankLinks.append(rrFolderId + "&productId="+productNode+"&ratingGroupName="+rrName+ordrBy);

		}
                List<DomainValue> rating = productNode.getRating();

		if (rating!=null && (prodPageRatingNames.size()>0 || prodPageTextRatingNames.size()>0) ) {
                    String ratingLabel = productNode.getRatingProdName();
                    if (ratingLabel==null) {
    			ratingLabel = "";
                    }
                    List<ProductRating> webRatings = new ArrayList<ProductRating>();
                    List<ProductRating> webTextRatings = new ArrayList<ProductRating>();
                
                    for (int i = 0, size = rating.size(); i < size; i++) {
                        if (rating.get(i) instanceof DomainValue) {
                        
                            DomainValue domainValue = (DomainValue) rating.get(i);
                            Domain domain = domainValue.getDomain();
                            String ratingName = domain.getName();
                            String ratingValueLabel = domain.getLabel();
            
                            if (prodPageRatingNames.contains(ratingName.toLowerCase())) {
                                ProductRating productRating = new ProductRating(ratingValueLabel, domainValue.getValue());
                                webRatings.add(productRating);
                            }
                            // check to see if this is one of the text ratings
                            if (prodPageTextRatingNames.contains(ratingName.toLowerCase())) {
                                ProductRating productRating = new ProductRating(ratingValueLabel, domainValue.getValue());
                                webTextRatings.add(productRating);
                            }
                        }
                    }
                    String sRateNRankLinks = rateNRankLinks!=null?rateNRankLinks.toString():null;
                    webRating = new WebProductRating(ratingLabel, webRatings, webTextRatings,rrFolderLabel, sRateNRankLinks);
		}
		return webRating;                                
	}
	
    static class RatingLinkBuilder {

        protected final ProductContainer productContainer;
        protected final StringBuilder rateNRankLinks = new StringBuilder();
        protected final HttpServletResponse response;

        public RatingLinkBuilder(ProductContainer productContainer, HttpServletResponse response) {
            this.productContainer = productContainer;
            this.response = response;
        }

        public String build() {
            String ratingGroupNames = productContainer.getRatingGroupNames();
            if (ratingGroupNames != null) {
                StringTokenizer stRRNames = new StringTokenizer(ratingGroupNames, ",");
                while (stRRNames.hasMoreTokens()) {
                    String rrName = stRRNames.nextToken().toUpperCase();

                    // go find the attribute with that name and it's label
                    List<Domain> ra = productContainer.getRatingDomain(rrName);
                    if (ra != null) {
                        if (ra.size() > 0) {
                            Domain raDMV = ra.get(0);
                            domain(raDMV);
                        } else {
                            domain(null);
                        }
                    } else {
                        domain(null);
                    }
                    ratingDomainLink(rrName);

                    // get the label for this rating group name.
                    String rrLabel = productContainer.getRatingGroupLabel(rrName);

                    ratingLabel(rrName, rrLabel);
                }
            }
            return rateNRankLinks.toString();
        }

        void ratingLabel(String rrName, String rrLabel) {
        }

        void ratingDomainLink(String rrName) {
        }

        void domain(Domain raDMV) {
        }
    }
    
    static class CategoryRatingLinkBuilder extends RatingLinkBuilder {
        String ordrBy ;
        
        public CategoryRatingLinkBuilder(ProductContainer productContainer, HttpServletResponse response) {
            super(productContainer, response);
        }

        @Override
        void domain(Domain raDMV) {
            if (raDMV != null) {
                ordrBy = "&orderBy=" + raDMV.getName().toLowerCase();
            } else {
                ordrBy = "&orderBy=price";
            }
        }

        @Override
        void ratingDomainLink(String rrName) {
            if (rateNRankLinks.length() > 1) {
                rateNRankLinks.append("<td class=\"text11bold\">&nbsp;|&nbsp;</td>");
            }

            rateNRankLinks.append("<td class=\"text11bold\"><a href=\"");
            rateNRankLinks.append(response.encodeURL("/rating_ranking.jsp?catId=" + productContainer + "&ratingGroupName=" + rrName + ordrBy));
            rateNRankLinks.append("\"><b>");
        }

        @Override
        void ratingLabel(String rrName, String rrLabel) {
            if (rrLabel != null) {
                rateNRankLinks.append(rrLabel);
            } else {
                rateNRankLinks.append((rrName.replace('_', ' ')));
            }
            rateNRankLinks.append("</b></a></td>");
        }
    }
    
    static class RatingRankingLinkBuilder extends RatingLinkBuilder {
        final String productIdURL;
        private String ordrBy;
        boolean isSameRatingGroup ;
        final private String ratingGroupName;

        public RatingRankingLinkBuilder(ProductContainer productContainer, HttpServletResponse response, String productIdURL, String ratingGroupName) {
            super(productContainer, response);
            this.productIdURL = productIdURL;
            this.ratingGroupName = ratingGroupName;
        }
        
        @Override
        void domain(Domain raDMV) {
            if (raDMV!=null) {
                ordrBy = "&orderBy="+raDMV.getName().toLowerCase();
            } else {
                ordrBy = "";
            }
        }

        @Override
        void ratingDomainLink(String rrName) {
            isSameRatingGroup = ratingGroupName.equalsIgnoreCase(rrName);
             
            if (rateNRankLinks.length() > 1) {
                rateNRankLinks.append(" | ");
            }
            if (!isSameRatingGroup) {
                rateNRankLinks.append("<a href=\"");
                rateNRankLinks.append(response
                        .encodeURL("/rating_ranking.jsp?catId=" + productContainer + "&ratingGroupName=" + rrName + ordrBy + productIdURL));
                rateNRankLinks.append("\">");
            }
        }
        
        @Override
        void ratingLabel(String rrName, String rrLabel) {
            if (rrLabel != null) {
                rateNRankLinks.append(rrLabel);
            } else {
                rateNRankLinks.append((rrName.replace('_', ' ')));
            }
            if (!isSameRatingGroup) {
                rateNRankLinks.append("</a>");
            }
        }
    }
    
    
    static class DepartmentRatingLinkBuilder extends RatingLinkBuilder {

        boolean hasRatingDomain;
        
        public DepartmentRatingLinkBuilder(ProductContainer productContainer, HttpServletResponse response) {
            super(productContainer, response);
        }
        
        @Override
        void domain(Domain raDMV) {
            hasRatingDomain = raDMV != null;
        }
        
        @Override
        void ratingDomainLink(String rrName) {
            if (hasRatingDomain) {
                if (rateNRankLinks.length() > 1) {
                    rateNRankLinks.append(" | ");
                }
                rateNRankLinks.append("<a href=\"");
                rateNRankLinks.append(response.encodeURL("/rating_ranking.jsp?deptId="+productContainer+"&ratingGroupName="+rrName));
                rateNRankLinks.append("\"");
            }
        }
        
        @Override
        void ratingLabel(String rrName, String rrLabel) {
            if (hasRatingDomain) {
                if (rrLabel!=null) {
                    rateNRankLinks.append(rrLabel);
                } else {
                  rateNRankLinks.append(rrName.replace('_',' '));
                }
                rateNRankLinks.append("</a>");
            }
        }
    }
    
    
    public static String buildCategoryRatingLink(ProductContainer productContainer, HttpServletResponse response) {
        return new CategoryRatingLinkBuilder(productContainer, response).build();
    }
    
    public static String buildRatingRankingLinkBuilder(ProductContainer productContainer, HttpServletResponse response, String productIdURL, String ratingGroupName) {
        return new RatingRankingLinkBuilder(productContainer, response, productIdURL, ratingGroupName).build();
    }
    
    public static String buildDepartmentRatingLink(ProductContainer productContainer, HttpServletResponse response) {
        return new DepartmentRatingLinkBuilder(productContainer, response).build();
    }
	
}
