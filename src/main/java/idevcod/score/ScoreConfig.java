package idevcod.score;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class ScoreConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScoreConfig.class);

    private String confPath;

    private Map<String, Integer> scoreMap = new HashMap<>();

    ScoreConfig(String confPath) {
        this.confPath = confPath;
    }

    void loadScoreConfig() {
        String scoreConfigPath = confPath + File.separator + "scoreConfig.xml";
        File file = new File(scoreConfigPath);
        if (!file.exists()) {
            throw new IllegalStateException("scoreConfig" + scoreConfigPath + " not found");
        }

        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(file);
            Element root = document.getRootElement();

            for (Iterator<Element> iterator = root.elementIterator("score"); iterator.hasNext(); ) {
                Element element = iterator.next();
                Score score = new Score(element.attributeValue("className"), element.attributeValue("method"),
                        Integer.valueOf(element.attributeValue("score")));
                scoreMap.put(key(score.getClassName(), score.getName()), score.getScore());
            }
        } catch (DocumentException e) {
            LOGGER.error("parse scoreConfig {} failed", scoreConfigPath, e);
            throw new IllegalStateException("parse scoreConfig " + scoreConfigPath + "failed");
        }
    }

    int getScore(String className, String methodName) {
        String key = key(className, methodName);
        if (!scoreMap.containsKey(key)) {
            LOGGER.error("score failed, test case {} not found score", key);
            throw new IllegalStateException("score failed, test case " + key + " not found score");
        }

        return scoreMap.get(key(className, methodName));
    }

    private String key(String className, String name) {
        return className + "#" + name;
    }

    Map<String, Integer> getScoreMap() {
        return Collections.unmodifiableMap(scoreMap);
    }
}
