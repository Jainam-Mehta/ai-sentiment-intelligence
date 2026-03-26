from flask import Flask, request, jsonify
from flask_cors import CORS
from transformers import pipeline
import logging
import os
import emoji

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app)

MODEL_NAME = "nlptown/bert-base-multilingual-uncased-sentiment"
logger.info(f"Loading model: {MODEL_NAME}")

def preprocess_text(text):
    """Convert emojis to text and clean up"""
    text_with_emojis = emoji.demojize(text)
    text_clean = text_with_emojis.replace(':', ' ')
    return text_clean

try:
    sentiment_pipeline = pipeline("sentiment-analysis", model=MODEL_NAME, tokenizer=MODEL_NAME)
    logger.info("Model loaded successfully")
    model_loaded = True
except Exception as e:
    logger.error(f"Failed to load model: {e}")
    model_loaded = False
    sentiment_pipeline = None

@app.route('/health', methods=['GET'])
def health():
    if model_loaded and sentiment_pipeline:
        return jsonify({'status': 'healthy', 'model': MODEL_NAME, 'message': 'AI service is ready'})
    else:
        return jsonify({'status': 'unhealthy', 'error': 'Model failed to load'}), 500

@app.route('/analyze', methods=['POST'])
def analyze_sentiment():
    try:
        data = request.get_json()
        if not data or 'text' not in data:
            return jsonify({'error': 'No text provided'}), 400
        
        raw_text = data['text'].strip()
        processed_text = preprocess_text(raw_text)
        
        if not processed_text:
            return jsonify({'error': 'Empty text provided'}), 400
        
        if not model_loaded or not sentiment_pipeline:
            return jsonify({'error': 'Model not available'}), 503
        
        result = sentiment_pipeline(processed_text)[0]
        label = result['label']
        score = result['score']
        
        if '5 star' in label or '4 star' in label or '5 stars' in label or '4 stars' in label:
            sentiment = 'positive'
        elif '3 star' in label or '3 stars' in label:
            sentiment = 'neutral'
        else:
            sentiment = 'negative'
        
        return jsonify({
            'sentiment': sentiment,
            'confidence': float(score),
            'original_label': label,
            'emoji_processed': processed_text != raw_text
        })
        
    except Exception as e:
        logger.error(f"Error: {e}")
        return jsonify({'error': str(e)}), 500

@app.route('/info', methods=['GET'])
def info():
    return jsonify({
        'model': MODEL_NAME,
        'features': ['emoji_support', 'sentiment_analysis'],
        'status': 'loaded' if model_loaded else 'failed'
    })

if __name__ == '__main__':
    port = int(os.environ.get('PORT', 7860))
    
    print("\n" + "="*50)
    print("AI SENTIMENT ANALYSIS SERVICE")
    print("="*50)
    print(f"Model: {MODEL_NAME}")
    print(f"Emoji Support: Enabled")
    print(f"Status: {'Loaded' if model_loaded else 'Failed'}")
    print(f"Server: http://0.0.0.0:{port}")
    print("="*50 + "\n")
    
    app.run(host='0.0.0.0', port=port, debug=False, use_reloader=False)