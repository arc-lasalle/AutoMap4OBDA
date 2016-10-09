/*
 * #%L
 * Simmetrics Core
 * %%
 * Copyright (C) 2014 - 2016 Simmetrics Authors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.simmetrics.tokenizers;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

/**
 * Convenience tokenizer. Provides default implementation to tokenize to set and
 * multiset.
 */
public abstract class AbstractTokenizer implements Tokenizer {

	@Override
	public Set<String> tokenizeToSet(final String input) {
		return new HashSet<>(tokenizeToList(input));
	}

	@Override
	public Multiset<String> tokenizeToMultiset(final String input) {
		return HashMultiset.create(tokenizeToList(input));
	}

}
