/*
 * Copyright 2024 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.langengine.pyexecutor;

/**
 * Builds the Python prelude script for persistent, session-based daemon processes.
 * <p>
 * This class generates a Python script that runs as a long-lived daemon. The script's
 * responsibilities include:
 * <ul>
 * <li>Initializing a secure environment based on policies passed via environment variables.</li>
 * <li>Sending a "ready" signal to the Java parent process upon successful startup.</li>
 * <li>Entering a loop to read execution commands (e.g., code to run, working directory) from stdin.</li>
 * <li>Executing the code within a persistent namespace ({@code NS}), preserving state across calls.</li>
 * <li>Reporting results and errors back to Java using the standard JSON protocol on stdout.</li>
 * <li>Handling exceptions during execution and gracefully restarting its state for the next command.</li>
 * </ul>
 */
public final class PreludeBuilderDaemon {
    private PreludeBuilderDaemon() {
    }

    /**
     * Renders the complete Python daemon script as a string.
     *
     * @param p The execution policy containing the rules to be embedded in the script.
     * @return A string containing the Python daemon script.
     */
    public static String render(PyExecutionPolicy p) {
        return ""
                + "import sys, os, json, builtins, ast\n"
                + "PROTO='[[PYEXEC]]'\n"
                + "print(PROTO+json.dumps({\"type\":\"_meta\",\"event\":\"ready\"})); sys.stdout.flush()\n"
                + "\n"
                + "CPU_LIMIT=int(os.getenv('PY_CPU_LIMIT','2')) if os.getenv('PY_CPU_LIMIT') else None\n"
                + "AS_LIMIT=int(os.getenv('PY_AS_LIMIT','0')) or None\n"
                + "FD_LIMIT=int(os.getenv('PY_FD_LIMIT','16')) if os.getenv('PY_FD_LIMIT') else None\n"
                + "DISABLE_NET = os.getenv('PY_DISABLE_NET','1')=='1'\n"
                + "OPEN_MODE   = os.getenv('PY_DISABLE_OPEN','1')\n"
                + "IMPORT_MODE = os.getenv('PY_IMPORT_MODE','WL')\n"
                + "BLOCK_DUNDER = os.getenv('PY_BLOCK_DUNDER','1')=='1'\n"
                + "PRINT_LAST = os.getenv('PY_PRINT_LAST_EXPR','1')=='1'\n"
                + "SESSION_MODE = os.getenv('PY_SESSION_MODE','0')=='1'\n"
                + "ALLOWED=set([x for x in os.getenv('PY_ALLOWED','').split(',') if x])\n"
                + "BANNED=set([x for x in os.getenv('PY_BANNED','').split(',') if x])\n"
                + "\n"
                + "try:\n"
                + "  import resource, signal\n"
                + "  if CPU_LIMIT is not None: resource.setrlimit(resource.RLIMIT_CPU,(CPU_LIMIT+1,CPU_LIMIT+1))\n"
                + "  if AS_LIMIT  is not None: resource.setrlimit(resource.RLIMIT_AS,(AS_LIMIT,AS_LIMIT))\n"
                + "  if FD_LIMIT  is not None: resource.setrlimit(resource.RLIMIT_NOFILE,(FD_LIMIT,FD_LIMIT))\n"
                + "  def _alarm(sig,frm):\n"
                + "    print(PROTO+json.dumps({\"type\":\"_meta\",\"event\":\"timeout\"})); sys.stdout.flush(); raise TimeoutError('timeout')\n"
                + "  try:\n"
                + "    signal.signal(signal.SIGALRM,_alarm)\n"
                + "  except Exception: pass\n"
                + "except Exception:\n"
                + "  resource=None\n"
                + "\n"
                + "if DISABLE_NET:\n"
                + "  try:\n"
                + "    import socket\n"
                + "    def _blocked(*a,**k): raise OSError('network disabled')\n"
                + "    socket.socket=_blocked; socket.create_connection=_blocked; socket.getaddrinfo=_blocked; socket.gethostbyname=_blocked\n"
                + "  except Exception: pass\n"
                + "\n"
                + "real_import=builtins.__import__\n"
                + "def guarded_import(name, globals=None, locals=None, fromlist=(), level=0):\n"
                + "  root=name.split('.')[0]\n"
                + "  if BLOCK_DUNDER and (root.startswith('__') or root.endswith('__')): raise ImportError('dunder blocked')\n"
                + "  if IMPORT_MODE=='WL':\n"
                + "    if root=='os' and SESSION_MODE: return real_import(name,globals,locals,fromlist,level)\n"
                + "    if root not in ALLOWED: raise ImportError(f\"import '{name}' not allowed\")\n"
                + "  else:\n"
                + "    if root in BANNED: raise ImportError(f\"import '{name}' banned\")\n"
                + "  return real_import(name,globals,locals,fromlist,level)\n"
                + "builtins.__import__=guarded_import\n"
                + "\n"
                + "if OPEN_MODE=='1':\n"
                + "  builtins.open=None\n"
                + "elif OPEN_MODE=='RO':\n"
                + "  _open=open\n"
                + "  def ro_open(path,mode='r',*a,**k):\n"
                + "    if any(x in mode for x in ('w','a','+','x')): raise PermissionError('write forbidden')\n"
                + "    base = os.path.realpath(os.getcwd())\n"
                + "    ap = os.path.realpath(os.path.join(base, path))\n"
                + "    if not (ap == base or ap.startswith(base + os.sep)):\n"
                + "      raise PermissionError('outside sandbox')\n"
                + "    return _open(ap,mode,*a,**k)\n"
                + "  builtins.open=ro_open\n"
                + "\n"
                + "NS={}\n"
                + "def run_code_ast(src, print_last=True):\n"
                + "  try:\n"
                + "    tree = ast.parse(src, '<cell>', 'exec')\n"
                + "    body = list(tree.body)\n"
                + "    if not body: return\n"
                + "    pre, last = body[:-1], body[-1]\n"
                + "    if 'signal' in globals():\n"
                + "      try:\n"
                + "        _t = int(CPU_LIMIT) if CPU_LIMIT is not None else 2\n"
                + "        globals()['signal'].alarm(max(1,_t))\n"
                + "      except Exception: pass\n"
                + "    if pre:\n"
                + "      exec(compile(ast.Module(body=pre, type_ignores=[]), '<cell>', 'exec'), NS, NS)\n"
                + "    if isinstance(last, ast.Expr) and print_last:\n"
                + "      val = eval(compile(ast.Expression(last.value), '<cell>', 'eval'), NS, NS)\n"
                + "      if val is not None:\n"
                + "        _r = repr(val)\n"
                + "        print(PROTO+json.dumps({\"type\":\"value\",\"repr\":_r})); sys.stdout.flush()\n"
                + "        return\n"
                + "    exec(compile(ast.Module(body=[last], type_ignores=[]), '<cell>', 'exec'), NS, NS)\n"
                + "  except Exception as e:\n"
                + "    _cls=getattr(type(e),'__name__','Exception')\n"
                + "    _msg=f\"{_cls}: {e}\"\n"
                + "    print(PROTO+json.dumps({\"type\":\"error\",\"error\":_msg})); sys.stdout.flush()\n"
                + "    # [FIX] Removed traceback logic, re-throwing exception directly.\n"
                + "    raise\n"
                + "  finally:\n"
                + "    try:\n"
                + "      if 'signal' in globals(): globals()['signal'].alarm(0)\n"
                + "    except Exception: pass\n"
                + "\n"
                + "for line in sys.stdin:\n"
                + "  line=line.strip()\n"
                + "  if not line: continue\n"
                + "  try: msg=json.loads(line)\n"
                + "  except Exception:\n"
                + "    print(PROTO+json.dumps({\"type\":\"error\",\"error\":\"bad json\"})); print(PROTO+json.dumps({\"type\":\"_done\"})); sys.stdout.flush(); continue\n"
                + "  op=msg.get('op'); opt=msg.get('opt') or {}\n"
                + "  if op=='shutdown': break\n"
                + "  if op=='exec':\n"
                + "    src=msg.get('code','')\n"
                + "    cwd=opt.get('cwd')\n"
                + "    if cwd:\n"
                + "      try: os.chdir(cwd)\n"
                + "      except Exception: print(PROTO+json.dumps({\"type\":\"error\",\"error\":\"invalid cwd\"}))\n"
                + "    pl = PRINT_LAST if opt.get('printLastExpression') is None else bool(opt.get('printLastExpression'))\n"
                + "    try:\n"
                + "      run_code_ast(src, pl)\n"
                + "    except Exception:\n"
                + "      import traceback, sys\n"
                + "      traceback.print_exc()\n"
                + "      # Explicitly signal a non-zero exit for this exec to the upper layer.\n"
                + "      print(PROTO+json.dumps({\"type\":\"_done\",\"exit\":1})); sys.stdout.flush()\n"
                + "      sys.exit(1)\n"
                + "    print(PROTO+json.dumps({\"type\":\"_done\",\"exit\":0})); sys.stdout.flush()\n"
                + "  else:\n"
                + "    print(PROTO+json.dumps({\"type\":\"error\",\"error\":\"unknown op\"})); print(PROTO+json.dumps({\"type\":\"_done\"})); sys.stdout.flush()\n";
    }
}