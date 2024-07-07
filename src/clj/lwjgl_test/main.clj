(ns lwjgl-test.main
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
          (GLFW/glfwSetKeyCallback w nil)
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

  (GL11/glClearColor 0.0 0.0 0.0 0.0)

  (while (not (GLFW/glfwWindowShouldClose @window-atom))
    (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT GL11/GL_DEPTH_BUFFER_BIT))

    (GL11/glColor3f 1.0 1.0 1.0)
    (GL11/glBegin GL11/GL_TRIANGLES)
    (GL11/glVertex2f -0.5 -0.5)
    (GL11/glVertex2f 0.5 -0.5)
    (GL11/glVertex2f 0.0 0.5)
    (GL11/glEnd)

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