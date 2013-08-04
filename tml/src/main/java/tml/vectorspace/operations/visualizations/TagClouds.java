/**
 * 
 */
package tml.vectorspace.operations.visualizations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import tml.vectorspace.operations.results.TagCloudsResult;

/**
 * @author Jorge
 *
 */
public class TagClouds extends AbstractVisualization {

	private int maxSizePixels = 24;
	private int maxResults = 50;
	
	public int getMaxSizePixels() {
		return maxSizePixels;
	}

	public void setMaxSizePixels(int maxSizePixels) {
		this.maxSizePixels = maxSizePixels;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getHTML() {
		List<TagCloudsResult> newResults = new ArrayList<TagCloudsResult>();
		int i=0;
		for(TagCloudsResult result : (List<TagCloudsResult>) operation.getResults()) {
			newResults.add(result);
			i++;
			if(i>maxResults)
				break;
		}
		Collections.sort(newResults,new Comparator<TagCloudsResult>() {
			@Override
			public int compare(TagCloudsResult o1, TagCloudsResult o2) {
				return o1.getTerm().compareTo(o2.getTerm());
			}
		});
		StringBuffer buffer = new StringBuffer();
		buffer.append("<div class=\"tml-tagcloud\">");
		for(TagCloudsResult result : newResults) {
			buffer.append("<font size=\"" + calculateSize(result.getWeight()) + "\">");
			buffer.append(result.getTerm());
			buffer.append("</font> ");
		}
		buffer.append("</div>");
		return buffer.toString();
	}

	private int calculateSize(double weight) {
		double size = (double) maxSizePixels;
		size = size * weight;
		return (int) size;
	}
}
