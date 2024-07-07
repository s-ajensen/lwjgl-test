(ns lwjgl-test.repl
  (:import [org.lwjgl.glfw Callbacks GLFW GLFWErrorCallback GLFWVidMode]
           [org.lwjgl.opengl GL GL11]
           [org.lwjgl.system MemoryStack MemoryUtil]))

(defn init [window-atom]
  (let [error-callback (GLFWErrorCallback/createPrint System/err)]
    (.set error-callback)
    (when-not (GLFW/glfwInit)
      (throw (IllegalStateException. "Unable to initialize GLFW")))
    (GLFW/glfwDefaultWindowHints)
    (GLFW/glfwWindowHint GLFW/GLFW_VISIBLE GLFW/GLFW_FALSE)
    (GLFW/glfwWindowHint GLFW/GLFW_RESIZABLE GLFW/GLFW_TRUE)
    (let [w (GLFW/glfwCreateWindow 300 300 "Hello World!" MemoryUtil/NULL MemoryUtil/NULL)]
      (if (= w 0)
        (throw (RuntimeException. "Failed to create the GLFW window"))
        (do
          (reset! window-atom w)
          (GLFW/glfwSetKeyCallback w nil #_(fn [window key scancode action mods]
                                       (when (and (= key GLFW/GLFW_KEY_ESCAPE)
                                                  (= action GLFW/GLFW_RELEASE))
                                         (GLFW/glfwSetWindowShouldClose window true))))
          (with-open [stack (MemoryStack/stackPush)]
            (let [pWidth (.mallocInt stack 1)
                  pHeight (.mallocInt stack 1)
                  vidmode (GLFW/glfwGetVideoMode (GLFW/glfwGetPrimaryMonitor))]
              (GLFW/glfwGetWindowSize w pWidth pHeight)
              (GLFW/glfwSetWindowPos w (/ (- (.width vidmode) (.get pWidth 0)) 2)
                                     (/ (- (.height vidmode) (.get pHeight 0)) 2))))
          (GLFW/glfwMakeContextCurrent w)
          (GLFW/glfwSwapInterval 1)
          (GLFW/glfwShowWindow w))))))

(defn loop-fn [window-atom]
  (GL/createCapabilities)
  (GL11/glClearColor 1.0 0.0 0.0 0.0)
  (while (not (GLFW/glfwWindowShouldClose @window-atom))
    (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT GL11/GL_DEPTH_BUFFER_BIT))
    (GLFW/glfwSwapBuffers @window-atom)
    (GLFW/glfwPollEvents)))

(defn -main []
  (let [window (atom nil)]
    (try
      (init window)
      (loop-fn window)
      (finally
        (Callbacks/glfwFreeCallbacks @window)
        (GLFW/glfwDestroyWindow @window)
        (GLFW/glfwTerminate)
        (.free (GLFW/glfwSetErrorCallback nil))))))