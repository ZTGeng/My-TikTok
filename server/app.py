from flask import Flask, jsonify, request, send_from_directory
import os, hashlib, shutil, cv2

app = Flask(__name__)

# video_map: a map from video ID to filename with extension
video_map = {}
# video_list: a list of video IDs
video_list = []

VIDEOS_DIR = 'videos'
THUMBNAILS_DIR = 'thumbnails'
THUMBNAIL_PLACEHOLDER = 'placeholder.jpg'

def generate_id(filename):
    return hashlib.md5(filename.encode('utf-8')).hexdigest()

def generate_thumbnail_from_video(id):
    filename = video_map.get(id)
    if filename is None:
        return False
    video_path = os.path.join(VIDEOS_DIR, filename)
    
    cap = cv2.VideoCapture(video_path)
    ret, frame = cap.read()
    cap.release()

    if ret:
        thumbnail_path = os.path.join(THUMBNAILS_DIR, id + '.jpg')
        cv2.imwrite(thumbnail_path, frame)
        return True
    else:
        return False

def generate_thumbnail(id):
    if os.path.isfile(os.path.join(THUMBNAILS_DIR, id + '.jpg')):
        return
    generated = generate_thumbnail_from_video(id)
    if not generated:
        shutil.copyfile(os.path.join(THUMBNAILS_DIR, THUMBNAIL_PLACEHOLDER), os.path.join(THUMBNAILS_DIR, id + '.jpg'))

def update_video_map():
    video_map.clear()
    video_list.clear()
    for filename in os.listdir(VIDEOS_DIR):
        if filename.endswith('.mp4'):
            id = generate_id(filename)
            video_map[id] = filename
            video_list.append(id)
            generate_thumbnail(id)

def get_next_available_name(filename):
    """
    filename: the name of the file without extension
    """
    existed_names = [video_map[id][:-4] for id in video_list]
    if filename not in existed_names:
        return filename
    n = 1
    while filename + ' (' + str(n) + ')' in existed_names:
        n += 1
    return filename + ' (' + str(n) + ')'

@app.route('/videos', methods=['GET'])
def get_videos():
    start = int(request.args.get('start', 0))
    limit = int(request.args.get('limit', 20))

    videos_to_return = []
    for id in video_list[start:start+limit]:
        thumbnail_path = os.path.join(THUMBNAILS_DIR, id + '.jpg')
        if id in video_map and os.path.isfile(thumbnail_path):
            videos_to_return.append({
                'id': id,
                'name': video_map[id].rsplit('.', 1)[0],
            })
    return jsonify({
        'status': 'success',
        'data': videos_to_return,
    })
    
@app.route('/video/<id>', methods=['GET'])
def serve_video(id):
    filename = video_map.get(id)
    if os.path.exists(os.path.join(VIDEOS_DIR, filename)):
        return send_from_directory(VIDEOS_DIR, filename)
    return jsonify({
        'status': 'error',
        'message': 'Video not found',
    })

@app.route('/thumbnail/<id>', methods=['GET'])
def serve_thumbnail(id):
    return send_from_directory(THUMBNAILS_DIR, id + '.jpg')

@app.route('/upload', methods=['POST'])
def upload_video():
    print("uploading")
    # Check if the request contains a file
    if 'file' not in request.files:
        print("no file")
        return jsonify({
            'status': 'error',
            'message': 'No file provided',
        }), 400

    # Get the file from the request
    file = request.files['file']

    # Check if the file type is valid
    if not file.filename.endswith('.mp4'):
        print("invalid file format, " + file.filename)
        return jsonify({
            'status': 'error',
            'message': 'Invalid file format',
        }), 400

    # Get the filename from the request parameters
    filename = request.form.get('filename')
    if filename is None:
        print("no filename")
        return jsonify({
            'status': 'error',
            'message': 'No filename provided',
        }), 400
    filename = get_next_available_name(filename) + '.mp4'

    # Generate an ID for the video
    id = generate_id(filename)

    print("saving")
    # Save the file to the videos directory
    file.save(os.path.join(VIDEOS_DIR, filename))

    # Update the video map and list
    video_map[id] = filename
    video_list.append(id)

    # Generate a thumbnail for the video
    generate_thumbnail(id)

    print('success')
    return jsonify({
        'status': 'success',
        'data': {
            'id': id,
            'name': filename,
        },
    }), 201

update_video_map()

if __name__ == '__main__':   
    app.run(host='0.0.0.0', port=5000, debug=True)