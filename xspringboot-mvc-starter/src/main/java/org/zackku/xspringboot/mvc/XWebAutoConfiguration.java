package org.zackku.xspringboot.mvc;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Zack
 * @date 2020/6/3
 */
@Configuration
@Import(XWebStartListener.class)
public class XWebAutoConfiguration {
}
