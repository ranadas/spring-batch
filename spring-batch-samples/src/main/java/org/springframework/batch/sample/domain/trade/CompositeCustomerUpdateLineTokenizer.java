/**
 * 
 */
package org.springframework.batch.sample.domain.trade;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.item.file.transform.LineTokenizer;

/**
 * Composite {@link LineTokenizer} that delegates the tokenization of a line to one of two potential
 * tokenizers.  The file format in this case uses one character, either F, A, U, or D to indicate 
 * whether or not the line is an a footer record, or a customer add, update, or delete, and 
 * will delegate accordingly.
 * 
 * @author Lucas Ward
 * @since 2.0
 */
public class CompositeCustomerUpdateLineTokenizer extends StepExecutionListenerSupport implements LineTokenizer {

	private LineTokenizer customerTokenizer;
	private LineTokenizer footerTokenizer;
	private StepExecution stepExecution;
	
	/* (non-Javadoc)
	 * @see org.springframework.batch.item.file.transform.LineTokenizer#tokenize(java.lang.String)
	 */
	public FieldSet tokenize(String line) {
		
		if(line.charAt(0) == 'F'){
			//line starts with F, so the footer tokenizer should tokenize it.
			FieldSet fs = footerTokenizer.tokenize(line);
			long customerUpdateTotal = stepExecution.getReadCount();
			long fileUpdateTotal = fs.readLong(1);
			if(customerUpdateTotal != fileUpdateTotal){
				throw new IllegalStateException("The total number of customer updates in the file footer does not match the " +
						"number entered  File footer total: [" + fileUpdateTotal + "] Total encountered during processing: [" +
						customerUpdateTotal + "]");
			}
			else{
				//return null, because the footer indicates an end of processing.
				return null;
			}
		}
		else if(line.charAt(0) == 'A' || line.charAt(0) == 'U' || line.charAt(0) == 'D'){
			//line starts with A,U, or D, so it must be a customer operation.
			return customerTokenizer.tokenize(line);
		}
		else{
			//If the line doesn't start with any of the characters above, it must obviously be invalid.
			throw new IllegalArgumentException("Invalid line encountered for tokenizing: " + line);
		}
	}
	
	@Override
	public void beforeStep(StepExecution stepExecution) {
		this.stepExecution = stepExecution;
	}

	/**
	 * Set the {@link LineTokenizer} that will be used to tokenize any lines that begin with
	 * A, U, or D, and are thus a customer operation.
	 * 
	 * @param customerTokenizer
	 */
	public void setCustomerTokenizer(LineTokenizer customerTokenizer) {
		this.customerTokenizer = customerTokenizer;
	}
	
	/**
	 * Set the {@link LineTokenizer} that will be used to tokenize any lines that being with
	 * F and is thus a footer record.
	 * 
	 * @param footerTokenizer
	 */
	public void setFooterTokenizer(LineTokenizer footerTokenizer) {
		this.footerTokenizer = footerTokenizer;
	}
}